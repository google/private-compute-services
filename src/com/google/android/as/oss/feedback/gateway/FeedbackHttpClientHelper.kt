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

package com.google.android.`as`.oss.feedback.gateway

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.SigningInfo
import com.google.common.flogger.GoogleLogger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Returns SHA1 for the application that is passed in network request.
 *
 * @param context the [Context]
 */
fun getCertFingerprint(context: Context): String? {
  return try {
    getCertFingerprint(
      context.packageManager
        .getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        .signingInfo
    )
  } catch (impossible: NameNotFoundException) {
    GoogleLogger.forEnclosingClass().atSevere().withCause(impossible).log("Package not found.")
    null // Return null in case of exception
  }
}

private fun getCertFingerprint(signingInfo: SigningInfo?): String? {
  val signatures = signingInfo?.apkContentsSigners
  if (signatures.isNullOrEmpty()) {
    return null
  }
  return try {
    val sha1 = MessageDigest.getInstance("SHA1")
    val digest = sha1.digest(signatures[0].toByteArray())
    bytesToHex(digest)
  } catch (impossible: NoSuchAlgorithmException) {
    GoogleLogger.forEnclosingClass().atSevere().withCause(impossible).log("SHA1 not found.")
    // Log exception
    null
  }
}

private fun bytesToHex(bytes: ByteArray): String {
  val hexChars = CharArray(bytes.size * 2)
  for (j in bytes.indices) {
    val v = bytes[j].toInt() and 0xFF
    hexChars[j * 2] = HEX_ARRAY[v ushr 4]
    hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
  }
  return String(hexChars)
}

private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
