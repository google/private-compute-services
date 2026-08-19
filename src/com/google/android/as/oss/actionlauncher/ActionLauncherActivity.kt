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

package com.google.android.`as`.oss.actionlauncher

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.`as`.oss.actionlauncher.config.ActionLauncherConfig
import com.google.android.`as`.oss.availability.api.agenticintegration.AgenticIntegrationServiceGrpcKt
import com.google.android.`as`.oss.availability.api.agenticintegration.ClientInfo
import com.google.android.`as`.oss.availability.api.agenticintegration.ErrorCode
import com.google.android.`as`.oss.availability.api.agenticintegration.account
import com.google.android.`as`.oss.availability.api.agenticintegration.clientInfo
import com.google.android.`as`.oss.availability.api.agenticintegration.launchRequest
import com.google.android.`as`.oss.availability.api.agenticintegration.textQuery
import com.google.android.`as`.oss.common.Executors.GENERAL_SINGLE_THREAD_EXECUTOR
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.common.time.TimeSource
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.android.AndroidLogTag
import com.google.fcp.client.common.GoogleSignatureVerifier
import com.google.protobuf.util.Durations
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.Metadata
import io.grpc.StatusException
import io.grpc.protobuf.lite.ProtoLiteUtils
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint(AppCompatActivity::class)
class ActionLauncherActivity : Hilt_ActionLauncherActivity() {
  private val dispatcher = GENERAL_SINGLE_THREAD_EXECUTOR.asCoroutineDispatcher()
  private val scope = CoroutineScope(dispatcher)
  @Inject lateinit var configReader: ConfigReader<ActionLauncherConfig>
  @Inject lateinit var stub: AgenticIntegrationServiceGrpcKt.AgenticIntegrationServiceCoroutineStub
  @Inject lateinit var timeSource: TimeSource
  @Inject lateinit var pccSecurityConfigReader: ConfigReader<PccSecurityConfig>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logger.atInfo().log("ActionLauncherActivity#onCreate")
    if (
      Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM ||
        !configReader.config.isActionLauncherEnabled
    ) {
      logger.atInfo().log("ActionLauncherActivity is not supported or disabled. Finishing.")
      finish()
      return
    }
    val geminiTextQuery = intent.getStringExtra(Constants.GEMINI_TEXT_QUERY_KEY)
    val shouldAutoSubmit = intent.getBooleanExtra(Constants.SHOULD_AUTO_SUBMIT_KEY, true)
    val shouldAutomate = intent.getBooleanExtra(Constants.SHOULD_AUTOMATE_KEY, false)
    val binder = intent.extras?.getBinder("EXTRA_SHARED_MEMORY_BINDER")

    scope.launch {
      if (binder != null) {
        val failureToastMessage = intent.getStringExtra(Constants.TOAST_MESSAGE_ON_FAILURE_KEY)
        val success = forwardBinderToTarget(intent, binder)
        logger.atInfo().log("ActionLauncherActivity#forwardBinderToTarget success: %s", success)
        if (!success) {
          if (!failureToastMessage.isNullOrEmpty()) {
            showToast(failureToastMessage)
          }
        }
      } else if (geminiTextQuery != null) {
        val toastMessageOnFailure = intent.getStringExtra(Constants.TOAST_MESSAGE_ON_FAILURE_KEY)
        val success =
          launchAgenticIntegrationServiceWithTextQuery(
            geminiTextQuery,
            shouldAutoSubmit,
            shouldAutomate,
            toastMessageOnFailure,
          )
        logger
          .atInfo()
          .log("ActionLauncherActivity#launchAgenticIntegrationServiceWithTextQuery: %s", success)
      }
      finish()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    logger.atInfo().log("ActionLauncherActivity#onDestroy")
    scope.cancel()
  }

  private suspend fun launchAgenticIntegrationServiceWithTextQuery(
    inputText: String,
    autoSubmit: Boolean,
    automate: Boolean,
    toastMessageOnFailure: String? = null,
  ): Boolean {
    val requestReceivedAt = Durations.fromMillis(timeSource.now().toEpochMilli())
    try {
      val request = launchRequest {
        clientInfo = getClientInfo()
        textQuery = textQuery {
          query = inputText
          shouldAutoSubmit = autoSubmit
          shouldAutomate = automate
        }
        requestStartTime = requestReceivedAt
      }
      val unused = stub.launch(request)
      return true
    } catch (e: Exception) {
      logger.atSevere().withCause(e).log("Error launching Agentic Integration Service")

      val errorCode = getAgenticIntegrationServiceLaunchErrorCode(e)
      logger.atSevere().log("Error code: %s", errorCode)
      toastMessageOnFailure?.let { if (it.isNotEmpty()) showToast(it) }
      return false
    }
  }

  private fun getClientInfo(): ClientInfo {
    val callingAppName = intent.getStringExtra(Constants.CLIENT_APP_NAME_KEY)
    val callingAppVersion = intent.getStringExtra(Constants.CLIENT_APP_VERSION_KEY)
    val callingPackageName = intent.getStringExtra(Constants.CLIENT_APP_PACKAGE_NAME_KEY)
    val accountName = intent.getStringExtra(Constants.ACCOUNT_NAME_KEY)
    return clientInfo {
      appName = callingAppName ?: ""
      appVersion = callingAppVersion ?: ""
      appPackageName = callingPackageName ?: ""
      if (!accountName.isNullOrEmpty()) {
        clientAccount += account { email = accountName }
      }
    }
  }

  private fun isSdkSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
  }

  private fun getAgenticIntegrationServiceLaunchErrorCode(e: Exception): ErrorCode.Code? {
    if (e !is StatusException) {
      return null
    }
    val trailers = e.trailers
    if (trailers != null) {
      val error = trailers.get(AGENTIC_INTEGRATION_SERVICE_LAUNCH_ERROR_KEY)
      if (error != null && error.hasCode()) {
        return error.code
      }
    }
    return null
  }

  @Suppress("DEPRECATION")
  private fun forwardBinderToTarget(incomingIntent: Intent, binder: IBinder): Boolean {
    val targetPackage = incomingIntent.getStringExtra("EXTRA_SHARE_TARGET_PACKAGE")
    val targetClass = incomingIntent.getStringExtra("EXTRA_SHARE_TARGET_CLASS")
    if (targetPackage.isNullOrEmpty() || targetClass.isNullOrEmpty()) {
      logger.atSevere().log("Target package or class is missing in incoming intent.")
      return false
    }
    if (!isValidTarget(targetPackage)) {
      logger.atSevere().log("Unauthorized target package: %s", targetPackage)
      return false
    }

    logger
      .atInfo()
      .log("Forwarding shared memory binder to target: %s/%s", targetPackage, targetClass)
    val bundle =
      Bundle(incomingIntent.extras).apply { putBinder("EXTRA_SHARED_MEMORY_BINDER", binder) }
    val intent =
      Intent(Constants.ACTION_SHARE_CONVERSATION).apply {
        setClassName(targetPackage, targetClass)
        putExtras(bundle)
      }
    return try {
      val options = ActivityOptions.makeBasic()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        options.setShareIdentityEnabled(true)
      }
      startActivityForResult(intent, REQUEST_CODE_FORWARD_TO_BLUEFLAX, options.toBundle())
      true
    } catch (e: Exception) {
      logger
        .atSevere()
        .withCause(e)
        .log("Failed to start target Activity %s/%s with binder", targetPackage, targetClass)
      false
    }
  }

  private fun isValidTarget(packageName: String): Boolean {
    if (packageName == "com.google.android.apps.pixel.blueflax") {
      val uid =
        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.getPackageUid(packageName, 0)
          } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).applicationInfo?.uid ?: return false
          }
        } catch (e: Exception) {
          return false
        }
      return SecurityPolicyUtils.isCallerAuthorized(
        pccSecurityConfigReader.config.securityInfoList(),
        this,
        uid,
        /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
      )
    }
    if (
      packageName == "com.google.android.googlequicksearchbox" ||
        packageName == "com.google.android.apps.bard"
    ) {
      if (Build.TYPE == "user") {
        return GoogleSignatureVerifier.getInstance(this).isPackageGoogleSigned(packageName)
      }
      return true
    }
    return false
  }

  private fun showToast(message: String) {
    this@ActionLauncherActivity.runOnUiThread {
      Toast.makeText(this@ActionLauncherActivity, message, Toast.LENGTH_LONG).show()
    }
  }

  companion object {
    private const val REQUEST_CODE_FORWARD_TO_BLUEFLAX = 1001

    @AndroidLogTag("ActionLauncherActivity") private val logger = GoogleLogger.forEnclosingClass()
    private val AGENTIC_INTEGRATION_SERVICE_LAUNCH_ERROR_KEY: Metadata.Key<ErrorCode> =
      Metadata.Key.of(
        "error-bin",
        ProtoLiteUtils.metadataMarshaller(ErrorCode.getDefaultInstance()),
      )
  }
}
