/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.`as`.oss.featurelauncher.service

import android.content.Context
import android.content.Intent as AndroidIntent
import androidx.activity.result.ActivityResult
import com.google.android.`as`.oss.featurelauncher.api.proto.ExecuteFeatureRequest
import com.google.android.`as`.oss.featurelauncher.api.proto.ExecuteFeatureResponse
import com.google.android.`as`.oss.featurelauncher.api.proto.Feature as ProtoFeature
import com.google.android.`as`.oss.featurelauncher.api.proto.FeatureResult
import com.google.android.`as`.oss.featurelauncher.api.proto.PcsFeatureLauncherServiceGrpcKt.PcsFeatureLauncherServiceCoroutineImplBase
import com.google.android.`as`.oss.featurelauncher.api.proto.executeFeatureResponse
import com.google.android.`as`.oss.featurelauncher.api.proto.featureResult
import com.google.common.flogger.GoogleLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.Status
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred

/**
 * Implementation of the Pcs Feature Launcher gRPC service.
 *
 * This service receives requests to launch different features, constructs the appropriate intents,
 * starts the launcher trampoline activity to execute the intents, and returns the result.
 */
internal class FeatureLauncherServiceImpl
@Inject
internal constructor(
  @ApplicationContext private val appContext: Context,
  private val launchIntentFactory: LaunchIntentFactory,
) : PcsFeatureLauncherServiceCoroutineImplBase() {

  override suspend fun executeFeature(request: ExecuteFeatureRequest): ExecuteFeatureResponse =
    try {
      executeFeatureInternal(request)
    } catch (e: Exception) {
      if (e is CancellationException) {
        throw e
      }
      logger.atSevere().withCause(e).log("Error in executeFeature")
      throw Status.UNKNOWN.withDescription("Internal error in feature launcher: ${e.message}")
        .withCause(e)
        .asException()
    }

  private suspend fun executeFeatureInternal(
    request: ExecuteFeatureRequest
  ): ExecuteFeatureResponse {
    val results = request.featureList.map { executeSingleFeature(it) }
    return executeFeatureResponse { featureResult += results }
  }

  private suspend fun executeSingleFeature(protoFeature: ProtoFeature): FeatureResult {
    return featureResult {
      this.feature = protoFeature
      val launchIntent =
        launchIntentFactory.createLaunchIntent(protoFeature)
          ?: run {
            this.status = FeatureResult.Status.FAILURE
            return@featureResult
          }

      launchIntent.addFlags(AndroidIntent.FLAG_ACTIVITY_NO_ANIMATION)

      try {
        val result = startTrampolineActivity(launchIntent)
        this.status = FeatureResult.Status.SUCCESS
        this.resultCode = result.resultCode
        result.data?.let { data ->
          val parcel = android.os.Parcel.obtain()
          data.writeToParcel(parcel, 0)
          this.parceledResult = com.google.protobuf.ByteString.copyFrom(parcel.marshall())
          parcel.recycle()
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        this.status = FeatureResult.Status.FAILURE
      }
    }
  }

  /**
   * Starts [FeatureLauncherTrampolineActivity] to execute the target [launchIntent] and receive its
   * result asynchronously.
   *
   * @param launchIntent The target intent for the actual feature we want to launch (e.g. VIP
   *   confirmation, Settings).
   * @return The [ActivityResult] of the target activity.
   */
  private suspend fun startTrampolineActivity(launchIntent: AndroidIntent): ActivityResult {
    val requestId = UUID.randomUUID().toString()
    val deferred = CompletableDeferred<ActivityResult>()
    pendingRequests[requestId] = deferred

    val trampolineIntent =
      AndroidIntent(appContext, FeatureLauncherTrampolineActivity::class.java).apply {
        putExtra(FeatureLauncherTrampolineActivity.EXTRA_TARGET_INTENT, launchIntent)
        putExtra(FeatureLauncherTrampolineActivity.EXTRA_REQUEST_ID, requestId)
        addFlags(AndroidIntent.FLAG_ACTIVITY_NEW_TASK or AndroidIntent.FLAG_ACTIVITY_NO_ANIMATION)
      }

    try {
      appContext.startActivity(trampolineIntent)
      return deferred.await()
    } catch (e: CancellationException) {
      val unused = pendingRequests.remove(requestId)
      logger.atInfo().log("Request cancelled, removed pending request: %b", unused != null)
      throw e
    } catch (e: Exception) {
      logger.atSevere().withCause(e).log("Failed to start activity or get result")
      val unused = pendingRequests.remove(requestId)
      logger.atInfo().log("Removed pending request on failure: %b", unused != null)
      throw e
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
    val pendingRequests = ConcurrentHashMap<String, CompletableDeferred<ActivityResult>>()
  }
}
