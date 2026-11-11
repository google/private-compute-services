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

package com.google.android.`as`.oss.sealedmemory.service.impl

import android.app.backup.BackupManager
import com.google.android.`as`.oss.sealedmemory.api.GetUserSecretByAssociationRequest
import com.google.android.`as`.oss.sealedmemory.api.GetUserSecretRequest
import com.google.android.`as`.oss.sealedmemory.api.GetUserSecretResponse
import com.google.android.`as`.oss.sealedmemory.api.SealedMemorySecretAccessServiceGrpcKt.SealedMemorySecretAccessServiceCoroutineImplBase
import com.google.android.`as`.oss.sealedmemory.api.getUserSecretResponse
import com.google.common.flogger.GoogleLogger
import com.google.protobuf.ByteString
import io.grpc.Status
import io.grpc.StatusException
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.cancellation.CancellationException

class SealedMemorySecretAccessService
@Inject
constructor(
  private val appSearchKeyStore: Provider<AppSearchKeyStore>,
  private val backupManager: BackupManager,
) : SealedMemorySecretAccessServiceCoroutineImplBase() {

  /** Retrieves the secret for the given key. */
  override suspend fun getUserSecret(request: GetUserSecretRequest): GetUserSecretResponse {
    try {
      val keyName = request.userIdentifier
      val keyStore = appSearchKeyStore.get()
      val encryptedKey = keyStore.retrieveKey(keyName)

      if (encryptedKey != null) {
        val secret =
          decryptSecret(encryptedKey) {
            keyStore.deleteKey(keyName)
            backupManager.dataChanged()
          }
        return getUserSecretResponse { userSecret = secret }
      } else {
        logger.atFine().log("getUserSecret: Secret not found for provided key")
        throw StatusException(Status.NOT_FOUND.withDescription("Secret not found for provided key"))
      }
    } catch (e: Exception) {
      if (e is CancellationException || e is StatusException) throw e
      logger.atSevere().withCause(e).log("Exception in getUserSecret for provided key")
      throw Status.INTERNAL.withDescription("Internal error in getUserSecret")
        .withCause(e)
        .asException()
    }
  }

  override suspend fun getUserSecretByAssociation(
    request: GetUserSecretByAssociationRequest
  ): GetUserSecretResponse {
    try {
      val cujIdentifier = request.cujIdentifier
      val keyStore = appSearchKeyStore.get()
      val keyName = keyStore.retrieveKeyAssociation(cujIdentifier)
      val encryptedKey = keyName?.let { keyStore.retrieveKey(it) }

      if (encryptedKey != null) {
        val secret =
          decryptSecret(encryptedKey) {
            keyStore.deleteKey(keyName)
            backupManager.dataChanged()
          }
        return getUserSecretResponse { userSecret = secret }
      } else {
        logger.atFine().log("getUserSecretByAssociation: Secret not found for provided CUJ")
        throw StatusException(Status.NOT_FOUND.withDescription("Secret not found for provided CUJ"))
      }
    } catch (e: Exception) {
      if (e is CancellationException || e is StatusException) throw e
      logger.atSevere().withCause(e).log("Exception in getUserSecretByAssociation for provided CUJ")
      throw Status.INTERNAL.withDescription("Internal error in getUserSecretByAssociation")
        .withCause(e)
        .asException()
    }
  }

  private suspend fun decryptSecret(
    encryptedKey: EncryptedKey,
    onIrrecoverableError: suspend () -> Unit,
  ): ByteString {
    return when (val decryptionResult = KeyGenerationUtil.decrypt(encryptedKey)) {
      is EncryptionResult.Success -> decryptionResult.value
      is EncryptionResult.IrrecoverableError -> {
        logger
          .atWarning()
          .withCause(decryptionResult.cause)
          .log("decryptSecret: Irrecoverable error for provided key; deleting unrecoverable key")
        onIrrecoverableError()
        throw Status.INTERNAL.withDescription("Failed to decrypt secret: Irrecoverable error")
          .withCause(decryptionResult.cause)
          .asException()
      }
      is EncryptionResult.RetryableError -> {
        logger
          .atWarning()
          .withCause(decryptionResult.cause)
          .log("decryptSecret: Retryable error for provided key")
        throw Status.UNAVAILABLE.withDescription("Failed to decrypt secret: Retryable error")
          .withCause(decryptionResult.cause)
          .asException()
      }
    }
  }

  private companion object {
    val logger = GoogleLogger.forEnclosingClass()
  }
}
