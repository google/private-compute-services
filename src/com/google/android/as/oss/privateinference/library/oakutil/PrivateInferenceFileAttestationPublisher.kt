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

package com.google.android.`as`.oss.privateinference.library.oakutil

import android.content.Context
import com.google.android.`as`.oss.common.time.TimeSource
import com.google.android.`as`.oss.privateinference.Annotations.AttestationPublisherExecutor
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceEndpointUrl
import com.google.common.flogger.GoogleLogger
import com.google.oak.attestation.v1.CollectedAttestation
import com.google.oak.attestation.v1.CollectedAttestation.RequestMetadata
import com.google.oak.session.AttestationPublisher
import com.google.oak.session.v1.EndorsedEvidence
import com.google.oak.session.v1.SessionBinding
import com.google.protobuf.ByteString
import com.google.protobuf.util.Timestamps
import java.io.File
import java.util.concurrent.Executor

/**
 * A simple attestation evidence publisher that writes to a file in the external files directory.
 *
 * For more information on the publisher, see [AttestationPublisherFlag].
 */
class PrivateInferenceFileAttestationPublisher(
  @param:AttestationPublisherExecutor private val executor: Executor,
  private val context: Context,
  private val timeSource: TimeSource,
  @PrivateInferenceEndpointUrl private val endpoint: String,
  private val subdir: String,
) : AttestationPublisher {

  override fun publish(
    evidence: Map<String, ByteArray>,
    bindings: Map<String, ByteArray>,
    handshakeHash: ByteArray?,
  ) {
    executor.execute {
      try {
        logger.atFine().log("Publishing attestation evidence to %s", subdir)
        val filename = "attestation_evidence_${timeSource.now().toEpochMilli()}.pb"
        val file = File(context.getExternalFilesDir(subdir), filename)
        val publishTime = timeSource.now()

        val collectedAttestation =
          CollectedAttestation.newBuilder()
            .apply {
              for ((key, value) in evidence) {
                putEndorsedEvidence(key, EndorsedEvidence.parseFrom(value))
              }
              for ((key, value) in bindings) {
                putSessionBindings(key, SessionBinding.parseFrom(value))
              }
              setHandshakeHash(ByteString.copyFrom(handshakeHash))

              setRequestMetadata(
                RequestMetadata.newBuilder()
                  .setUri(endpoint)
                  .setRequestTime(Timestamps.fromMillis(publishTime.toEpochMilli()))
              )
            }
            .build()

        file.writeBytes(collectedAttestation.toByteArray())

        logger.atFine().log("Published collected attestation to %s", file)
      } catch (e: Exception) {
        logger.atWarning().withCause(e).log("Failed to publish attestation evidence")
      }
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
