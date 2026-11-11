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
import com.google.android.`as`.oss.sealedmemory.api.CreateKeyAssociationRequest
import com.google.android.`as`.oss.sealedmemory.api.CreateKeyAssociationResponse
import com.google.android.`as`.oss.sealedmemory.api.CreateUserSecretRequest
import com.google.android.`as`.oss.sealedmemory.api.CreateUserSecretResponse
import com.google.android.`as`.oss.sealedmemory.api.DeleteKeyAssociationRequest
import com.google.android.`as`.oss.sealedmemory.api.DeleteKeyAssociationResponse
import com.google.android.`as`.oss.sealedmemory.api.DeleteUserSecretRequest
import com.google.android.`as`.oss.sealedmemory.api.DeleteUserSecretResponse
import com.google.android.`as`.oss.sealedmemory.api.DoesUserSecretExistRequest
import com.google.android.`as`.oss.sealedmemory.api.DoesUserSecretExistResponse
import com.google.android.`as`.oss.sealedmemory.api.SealedMemorySecretManagementServiceGrpcKt.SealedMemorySecretManagementServiceCoroutineImplBase
import com.google.android.`as`.oss.sealedmemory.api.UpdateKeyAssociationRequest
import com.google.android.`as`.oss.sealedmemory.api.UpdateKeyAssociationResponse
import com.google.android.`as`.oss.sealedmemory.api.createKeyAssociationResponse
import com.google.android.`as`.oss.sealedmemory.api.createUserSecretResponse
import com.google.android.`as`.oss.sealedmemory.api.deleteKeyAssociationResponse
import com.google.android.`as`.oss.sealedmemory.api.deleteUserSecretResponse
import com.google.android.`as`.oss.sealedmemory.api.doesUserSecretExistResponse
import com.google.android.`as`.oss.sealedmemory.api.updateKeyAssociationResponse
import com.google.common.flogger.GoogleLogger
import com.google.protobuf.ByteString
import io.grpc.Status
import io.grpc.StatusException
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.cancellation.CancellationException

class SealedMemorySecretManagementService
@Inject
constructor(
  private val appSearchKeyStore: Provider<AppSearchKeyStore>,
  private val backupManager: BackupManager,
) : SealedMemorySecretManagementServiceCoroutineImplBase() {

  /**
   * Checks if a valid, decryptable secret exists for the given key. If an existing entry is stored
   * in [AppSearchKeyStore] but cannot be decrypted due to an irrecoverable error (for example, when
   * the AndroidKeyStore master key was wiped), it is deleted and `false` is returned so callers can
   * recreate the secret.
   */
  override suspend fun doesUserSecretExist(
    request: DoesUserSecretExistRequest
  ): DoesUserSecretExistResponse {
    try {
      val exists = hasValidSecret(request.userIdentifier)
      logger.atFine().log("doesUserSecretExist for provided key %s", exists)
      return doesUserSecretExistResponse { this.exists = exists }
    } catch (e: Exception) {
      if (e is CancellationException || e is StatusException) throw e
      logger.atSevere().withCause(e).log("Exception in doesUserSecretExist for provided key")
      throw Status.INTERNAL.withDescription("Internal error in doesUserSecretExist")
        .withCause(e)
        .asException()
    }
  }

  /**
   * Creates the secret for the given key. Returns `success = false` if a valid decryptable secret
   * already exists, or overwrites an existing entry if it is irrecoverably undecryptable.
   */
  override suspend fun createUserSecret(
    request: CreateUserSecretRequest
  ): CreateUserSecretResponse {
    try {
      val keyName = request.userIdentifier

      if (hasValidSecret(keyName)) {
        logger.atFine().log("createUserSecret: Secret already exists for provided key")
        return createUserSecretResponse { success = false }
      }

      val newSecret = KeyGenerationUtil.createRandomKey()
      val newEncryptedKey = encryptSecret(newSecret, keyName)

      appSearchKeyStore.get().storeKey(keyName, newEncryptedKey)
      backupManager.dataChanged()

      logger.atFine().log("createUserSecret: Created secret for provided key")
      return createUserSecretResponse { success = true }
    } catch (e: Exception) {
      if (e is CancellationException || e is StatusException) throw e
      logger.atSevere().withCause(e).log("Exception in createUserSecret for provided key")
      throw Status.INTERNAL.withDescription("Internal error in createUserSecret")
        .withCause(e)
        .asException()
    }
  }

  /** Deletes the secret for the given key. */
  override suspend fun deleteUserSecret(
    request: DeleteUserSecretRequest
  ): DeleteUserSecretResponse {
    try {
      val keyName = request.userIdentifier
      val existingKey = appSearchKeyStore.get().retrieveKey(keyName)
      var wasDeleted = false
      if (existingKey != null) {
        appSearchKeyStore.get().deleteKey(keyName)
        wasDeleted = true
        backupManager.dataChanged()
      }

      logger.atFine().log("deleteUserSecret for provided key: %s", wasDeleted)
      return deleteUserSecretResponse { this.wasDeleted = wasDeleted }
    } catch (e: Exception) {
      if (e is StatusException) throw e
      logger.atSevere().withCause(e).log("Exception in deleteUserSecret for provided key")
      throw Status.INTERNAL.withDescription("Internal error in deleteUserSecret")
        .withCause(e)
        .asException()
    }
  }

  override suspend fun createKeyAssociation(
    request: CreateKeyAssociationRequest
  ): CreateKeyAssociationResponse {
    try {
      val success =
        appSearchKeyStore.get().createKeyAssociation(request.cujIdentifier, request.userIdentifier)
      logger.atFine().log("createKeyAssociation for provided CUJ: %s", success)
      return createKeyAssociationResponse { this.success = success }
    } catch (e: Exception) {
      if (e is StatusException) throw e
      logger.atSevere().withCause(e).log("Exception in createKeyAssociation for provided CUJ")
      throw Status.INTERNAL.withDescription("Internal error in createKeyAssociation")
        .withCause(e)
        .asException()
    }
  }

  override suspend fun updateKeyAssociation(
    request: UpdateKeyAssociationRequest
  ): UpdateKeyAssociationResponse {
    try {
      val success =
        appSearchKeyStore
          .get()
          .updateKeyAssociation(request.cujIdentifier, request.newUserIdentifier)
      logger.atFine().log("updateKeyAssociation for provided CUJ: %s", success)
      return updateKeyAssociationResponse { this.success = success }
    } catch (e: Exception) {
      if (e is StatusException) throw e
      logger.atSevere().withCause(e).log("Exception in updateKeyAssociation for provided CUJ")
      throw Status.INTERNAL.withDescription("Internal error in updateKeyAssociation")
        .withCause(e)
        .asException()
    }
  }

  override suspend fun deleteKeyAssociation(
    request: DeleteKeyAssociationRequest
  ): DeleteKeyAssociationResponse {
    try {
      val wasDeleted = appSearchKeyStore.get().deleteKeyAssociation(request.cujIdentifier)
      logger.atFine().log("deleteKeyAssociation for provided CUJ: %s", wasDeleted)
      return deleteKeyAssociationResponse { this.wasDeleted = wasDeleted }
    } catch (e: Exception) {
      if (e is StatusException) throw e
      logger.atSevere().withCause(e).log("Exception in deleteKeyAssociation for provided CUJ")
      throw Status.INTERNAL.withDescription("Internal error in deleteKeyAssociation")
        .withCause(e)
        .asException()
    }
  }

  private suspend fun hasValidSecret(keyName: String): Boolean {
    val keyStore = appSearchKeyStore.get()
    val encryptedKey = keyStore.retrieveKey(keyName) ?: return false
    return when (val decryptionResult = KeyGenerationUtil.decrypt(encryptedKey)) {
      is EncryptionResult.Success -> true
      is EncryptionResult.IrrecoverableError -> {
        logger
          .atWarning()
          .withCause(decryptionResult.cause)
          .log("hasValidSecret: Irrecoverable error for provided key; deleting unrecoverable key")
        keyStore.deleteKey(keyName)
        backupManager.dataChanged()
        false
      }
      is EncryptionResult.RetryableError -> {
        logger
          .atWarning()
          .withCause(decryptionResult.cause)
          .log("hasValidSecret: Retryable error for provided key")
        throw Status.UNAVAILABLE.withDescription("Failed to verify secret: Retryable error")
          .withCause(decryptionResult.cause)
          .asException()
      }
    }
  }

  private fun encryptSecret(secret: ByteString, userIdentifier: String): EncryptedKey {
    return when (val encryptionResult = KeyGenerationUtil.encrypt(secret)) {
      is EncryptionResult.Success -> encryptionResult.value
      is EncryptionResult.RetryableError -> {
        logger
          .atWarning()
          .withCause(encryptionResult.cause)
          .log("encryptSecret: Retryable error for provided key")
        throw Status.UNAVAILABLE.withDescription("Failed to encrypt new secret: Retryable error")
          .withCause(encryptionResult.cause)
          .asException()
      }
      is EncryptionResult.IrrecoverableError -> {
        logger
          .atWarning()
          .withCause(encryptionResult.cause)
          .log("encryptSecret: Irrecoverable error for provided key")
        throw Status.INTERNAL.withDescription("Failed to encrypt new secret: Irrecoverable error")
          .withCause(encryptionResult.cause)
          .asException()
      }
    }
  }

  private companion object {
    val logger = GoogleLogger.forEnclosingClass()
  }
}
