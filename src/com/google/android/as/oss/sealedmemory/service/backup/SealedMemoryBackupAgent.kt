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

package com.google.android.`as`.oss.sealedmemory.service.backup

import android.app.backup.BackupAgent
import android.app.backup.BackupAgent.FLAG_CLIENT_SIDE_ENCRYPTION_ENABLED
import android.app.backup.BackupAgent.FLAG_DEVICE_TO_DEVICE_TRANSFER
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.BackupManager
import android.app.backup.BackupRestoreEventLogger
import android.os.Build
import android.os.ParcelFileDescriptor
import com.google.android.`as`.oss.sealedmemory.service.backup.SealedMemoryBackupPayloadKt.associationEntry
import com.google.android.`as`.oss.sealedmemory.service.backup.SealedMemoryBackupPayloadKt.keyEntry
import com.google.android.`as`.oss.sealedmemory.service.impl.AppSearchKeyStore
import com.google.android.`as`.oss.sealedmemory.service.impl.EncryptedKey
import com.google.android.`as`.oss.sealedmemory.service.impl.EncryptionResult
import com.google.android.`as`.oss.sealedmemory.service.impl.KeyGenerationUtil
import com.google.common.flogger.GoogleLogger
import com.google.protobuf.ByteString
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

/** [BackupAgent] for Sealed Memory keys */
class SealedMemoryBackupAgent : BackupAgent() {
  private lateinit var appSearchKeyStore: AppSearchKeyStore
  private lateinit var eventLogger: BackupEventLoggerHelper

  override fun onCreate() {
    val entryPoint =
      EntryPoints.get(applicationContext, SealedMemoryBackupAgentEntryPoint::class.java)
    appSearchKeyStore = entryPoint.getAppSearchKeyStore()

    eventLogger = BackupEventLoggerHelper(this)
  }

  override fun onBackup(
    oldState: ParcelFileDescriptor,
    data: BackupDataOutput,
    newState: ParcelFileDescriptor,
  ): Unit = runBlocking {
    // Only perform backup if client-side encryption is enabled or if this is a direct
    // device-to-device (D2D) transfer. Cloud backups without client-side encryption (such as when
    // the user has not configured a secure lock screen PIN/pattern/password backed by a Lock Screen
    // Knowledge Factor (LSKF) / hardware security module) must never export raw Sealed Memory
    // keys and associations off-device in plaintext.
    if (
      (data.transportFlags and
        (FLAG_CLIENT_SIDE_ENCRYPTION_ENABLED or FLAG_DEVICE_TO_DEVICE_TRANSFER)) == 0
    ) {
      logger.atWarning().log("Skipping backup because client-side encryption is not enabled.")
      return@runBlocking
    }
    try {
      val allKeys = appSearchKeyStore.retrieveAllKeys()
      val allAssociations = appSearchKeyStore.retrieveAllKeyAssociations()
      var numKeysBackedUp = 0
      var numKeysFailed = 0

      val backupProto = sealedMemoryBackupPayload {
        for ((name, key) in allKeys) {
          val result = KeyGenerationUtil.decrypt(key)
          val decryptedKey =
            when (result) {
              is EncryptionResult.Success<ByteString> -> result.value
              is EncryptionResult.RetryableError,
              is EncryptionResult.IrrecoverableError -> {
                logger.atSevere().log("Failed to decrypt key")
                numKeysFailed++
                continue
              }
            }
          keys.add(
            keyEntry {
              keyName = name
              keyData = decryptedKey
            }
          )
          numKeysBackedUp++
        }
        for ((id, name) in allAssociations) {
          associations.add(
            associationEntry {
              associationId = id
              keyName = name
            }
          )
        }
      }

      eventLogger.logItemsBackedUp(DATA_TYPE_KEYS, numKeysBackedUp)
      if (numKeysFailed > 0) {
        eventLogger.logItemsBackupFailed(DATA_TYPE_KEYS, numKeysFailed, "Decryption failed")
      }
      eventLogger.logItemsBackedUp(DATA_TYPE_ASSOCIATIONS, allAssociations.size)

      val backupData: ByteArray = backupProto.toByteArray()
      data.writeEntityHeader(ENTITY_KEY, backupData.size)
      data.writeEntityData(backupData, backupData.size)
      eventLogger.logItemsBackedUp(DATA_TYPE_BACKUP, count = 1)
    } catch (e: Exception) {
      logger.atSevere().withCause(e).log("Failed to perform backup")
      eventLogger.logItemsBackupFailed(DATA_TYPE_BACKUP, count = 1, error = null)
    }
  }

  override fun onRestore(
    data: BackupDataInput,
    appVersionCode: Int,
    newState: ParcelFileDescriptor,
  ): Unit = runBlocking {
    try {
      while (data.readNextHeader()) {
        if (data.key == ENTITY_KEY) {
          val dataSize = data.dataSize
          val backupData = ByteArray(dataSize)
          data.readEntityData(backupData, 0, dataSize)

          val backupProto = SealedMemoryBackupPayload.parseFrom(backupData)
          var numKeysRestored = 0
          var numKeysFailed = 0
          for (keyEntry in backupProto.keysList) {
            val result = KeyGenerationUtil.encrypt(keyEntry.keyData)
            val encryptedKey =
              when (result) {
                is EncryptionResult.Success<EncryptedKey> -> result.value
                is EncryptionResult.RetryableError,
                is EncryptionResult.IrrecoverableError -> {
                  logger.atSevere().log("Failed to encrypt key: %s", keyEntry.keyName)
                  numKeysFailed++
                  continue
                }
              }
            appSearchKeyStore.storeKey(keyEntry.keyName, encryptedKey)
            numKeysRestored++
          }
          eventLogger.logItemsRestored(DATA_TYPE_KEYS, numKeysRestored)
          if (numKeysFailed > 0) {
            eventLogger.logItemsRestoreFailed(DATA_TYPE_KEYS, numKeysFailed, "Encryption failed")
          }

          var numAssociationsRestored = 0
          for (associationEntry in backupProto.associationsList) {
            // This will not overwrite as currently implemented, if the associationId exists.
            val unused =
              appSearchKeyStore.createKeyAssociation(
                associationEntry.associationId,
                associationEntry.keyName,
              )
            numAssociationsRestored++
          }
          eventLogger.logItemsRestored(DATA_TYPE_ASSOCIATIONS, numAssociationsRestored)
          eventLogger.logItemsRestored(DATA_TYPE_BACKUP, count = 1)
        } else {
          data.skipEntityData()
        }
      }
    } catch (e: Exception) {
      logger.atSevere().withCause(e).log("Failed to perform restore")
      eventLogger.logItemsRestoreFailed(DATA_TYPE_BACKUP, count = 1, error = null)
    }
  }

  @EntryPoint
  @InstallIn(SingletonComponent::class)
  interface SealedMemoryBackupAgentEntryPoint {
    fun getAppSearchKeyStore(): AppSearchKeyStore
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    private const val ENTITY_KEY = "sm_backup_key"
    private const val DATA_TYPE_BACKUP = "sm_backup"
    private const val DATA_TYPE_KEYS = "sm_keys"
    private const val DATA_TYPE_ASSOCIATIONS = "sm_associations"
  }
}

/** Helper class to wrap [BackupRestoreEventLogger] for API compatibility. */
private class BackupEventLoggerHelper(backupAgent: BackupAgent) {
  private var eventLogger: BackupRestoreEventLogger? = null

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      try {
        eventLogger = BackupManager(backupAgent).getBackupRestoreEventLogger(backupAgent)
      } catch (e: Exception) {
        GoogleLogger.forEnclosingClass()
          .atWarning()
          .withCause(e)
          .log("Could not get backup restore event logger")
      }
    }
  }

  fun logItemsBackedUp(dataType: String, count: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      eventLogger?.logItemsBackedUp(dataType, count)
    }
  }

  fun logItemsBackupFailed(dataType: String, count: Int, error: String?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      eventLogger?.logItemsBackupFailed(dataType, count, error)
    }
  }

  fun logItemsRestored(dataType: String, count: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      eventLogger?.logItemsRestored(dataType, count)
    }
  }

  fun logItemsRestoreFailed(dataType: String, count: Int, error: String?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      eventLogger?.logItemsRestoreFailed(dataType, count, error)
    }
  }
}
