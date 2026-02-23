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

package com.google.android.`as`.oss.privateinference.library.bsa.token.crypto

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.google.android.`as`.oss.common.ExecutorAnnotations.PiTokenEncryptionExecutorQualifier
import com.google.android.`as`.oss.common.initializer.PcsInitializer
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenBytes
import com.google.common.flogger.GoogleLogger
import com.google.common.util.concurrent.ListeningScheduledExecutorService
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.TinkProtoKeysetFormat
import com.google.crypto.tink.aead.AeadConfigurationV0
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.integration.android.AndroidKeystore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import javax.crypto.BadPaddingException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.guava.await
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString

/**
 * Implementation of [BsaTokenCipher] using Tink AEAD encryption.
 *
 * Much of the structure of this follows official Tink examples, but we make the explicit choice to
 * return `null` rather than throw an exception when tokens can't be encrypted nor decrypted
 * successfully: having the effect of causing cache misses rather than catastrophic failure.
 *
 * @param executor Executor to run Tink operations on. Must be single-threaded for safety.
 */
@Singleton
class TinkBsaTokenCipher
@Inject
constructor(
  @ApplicationContext private val context: Context,
  @PiTokenEncryptionExecutorQualifier private val executor: ListeningScheduledExecutorService,
) : BsaTokenCipher {
  @VisibleForTesting val aeadState = MutableStateFlow<AeadState>(AeadState.Uninitialized)

  internal fun initialize() {
    logger.atFine().log("initialize: submitting task to executor")
    executor.execute {
      logger.atFine().log("initialize: executing task")
      TinkConfig.register()
      try {
        val handle = getOrCreateEncryptedKeysetWithRetry()
        val aead = handle.getPrimitive(AeadConfigurationV0.get(), Aead::class.java)
        aeadState.value = AeadState.Initialized(aead)
        logger.atInfo().log("Initialized successfully")
      } catch (e: Exception) {
        logger.atSevere().withCause(e).log("Unable to initialize Tink AEAD")
        aeadState.value = AeadState.Failed(e)
      }
    }
  }

  override suspend fun encrypt(tokenBytes: BsaTokenBytes): EncryptedBsaTokenBytes? {
    val aead =
      try {
        aeadState
          .onEach { if (it is AeadState.Failed) throw it.error }
          .mapNotNull { it as? AeadState.Initialized }
          .first()
          .aead
      } catch (e: Throwable) {
        logger.atWarning().withCause(e).log("Could not encrypt BsaTokenBytes")
        return null
      }
    return executor
      .submit<EncryptedBsaTokenBytes?> {
        try {
          EncryptedBsaTokenBytes(
            encryptedData = aead.encrypt(tokenBytes.toByteArray(), TOKEN_ASSOCIATED_DATA),
            associatedData = TOKEN_ASSOCIATED_DATA,
          )
        } catch (e: GeneralSecurityException) {
          logger.atWarning().withCause(e).log("Could not encrypt BsaTokenBytes")
          null
        }
      }
      .await()
  }

  override suspend fun decrypt(encryptedTokenBytes: EncryptedBsaTokenBytes): BsaTokenBytes? {
    val aead =
      try {
        aeadState
          .onEach { if (it is AeadState.Failed) throw it.error }
          .mapNotNull { it as? AeadState.Initialized }
          .first()
          .aead
      } catch (e: Throwable) {
        logger.atWarning().withCause(e).log("Could not decrypt EncryptedBsaTokenBytes")
        return null
      }
    return executor
      .submit<BsaTokenBytes?> {
        try {
          BsaTokenBytes(
            aead.decrypt(
              encryptedTokenBytes.encryptedData.toByteArray(),
              encryptedTokenBytes.associatedData.toByteArray(),
            )
          )
        } catch (e: GeneralSecurityException) {
          logger.atWarning().withCause(e).log("Could not decrypt BsaTokenBytes")
          null
        }
      }
      .await()
  }

  private fun getOrCreateEncryptedKeysetWithRetry(): KeysetHandle {
    // Add retry logic in case of transient Android Keystore errors.
    var retries = 3
    var maxWaitTimeMillis = 100
    while (true) {
      try {
        return getOrCreateEncryptedKeyset()
      } catch (e: Exception) {
        logger
          .atFine()
          .withCause(e)
          .log("Exception during getOrCreateEncryptedKeyset, %d retries left", retries)
        if (retries <= 0) throw e
      }
      sleepRandomAmount(maxWaitTimeMillis)
      retries--
      maxWaitTimeMillis *= 2
    }
  }

  private fun sleepRandomAmount(maxWaitTimeMillis: Int) {
    val waitTimeMillis = (Math.random() * maxWaitTimeMillis).toInt()
    try {
      Thread.sleep(waitTimeMillis.toLong())
    } catch (_: InterruptedException) {
      // Ignored.
    }
  }

  private fun getOrCreateEncryptedKeyset(triesLeft: Int = 3): KeysetHandle {
    val sharedPreferences: SharedPreferences =
      context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    val encryptedKeysetExists = sharedPreferences.contains(TINK_KEYSET_NAME)
    val keysetEncryptionKeyExists = AndroidKeystore.hasKey(KEY_ENCRYPTION_KEY_ALIAS)
    logger
      .atFine()
      .log(
        "getOrCreateEncryptedKeyset: encryptedKeysetExists: %s, keysetEncryptionKeyExists: %s",
        encryptedKeysetExists,
        keysetEncryptionKeyExists,
      )

    if (encryptedKeysetExists && keysetEncryptionKeyExists) {
      try {
        // Read the encrypted keyset from the shared preferences and decrypt it.
        val encryptedKeyset =
          sharedPreferences.getString(TINK_KEYSET_NAME, "")?.decodeBase64()?.toByteArray()
        return TinkProtoKeysetFormat.parseEncryptedKeyset(
          encryptedKeyset,
          AndroidKeystore.getAead(KEY_ENCRYPTION_KEY_ALIAS),
          TINK_KEYSET_ASSOCIATED_DATA,
        )
      } catch (e: BadPaddingException) {
        // Note that {@link BadPaddingException} includes {@link AEADBadTagException}.
        // This may happen if the encrypted keyset is corrupted, or if it was encrypted
        // with a different KEK. We will create a new keyset and return that instead.
        if (triesLeft <= 0) throw e
      }
    }

    logger.atFine().log("getOrCreateEncryptedKeyset: Creating encrypted keyset")
    // Create the keyset and store it
    val keyset = createEncryptedKeyset()
    sharedPreferences.edit(commit = true) {
      putString(TINK_KEYSET_NAME, keyset.toByteString().base64())
    }

    // Try to fetch it by recursing.
    return getOrCreateEncryptedKeyset(triesLeft = triesLeft - 1)
  }

  private fun createEncryptedKeyset(): ByteArray {
    // Create a new KEK in Android Keystore, or overwrite it if one already exists.
    AndroidKeystore.generateNewAes256GcmKey(KEY_ENCRYPTION_KEY_ALIAS)
    val handle = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM)
    // Encrypt the keyset with the KEK.
    return TinkProtoKeysetFormat.serializeEncryptedKeyset(
      handle,
      AndroidKeystore.getAead(KEY_ENCRYPTION_KEY_ALIAS),
      TINK_KEYSET_ASSOCIATED_DATA,
    )
  }

  @Singleton
  class Initializer @Inject constructor(private val cipher: TinkBsaTokenCipher) : PcsInitializer {
    override fun run() = cipher.initialize()
  }

  @VisibleForTesting
  sealed class AeadState {
    data object Uninitialized : AeadState()

    data class Initialized(val aead: Aead) : AeadState()

    data class Failed(val error: Throwable) : AeadState()
  }

  companion object {
    private const val PREF_FILE_NAME = "pi_bsa_tink_encrypted_keyset_pref"
    private const val TINK_KEYSET_NAME = "pi_bsa_tink_encrypted_keyset"
    private const val KEY_ENCRYPTION_KEY_ALIAS = "pi_bsa_tink_encrypted_keyset_kek"
    private val TINK_KEYSET_ASSOCIATED_DATA = "pi bsa tink associated data".toByteArray()
    private val TOKEN_ASSOCIATED_DATA = "pi bsa token".toByteArray()
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
