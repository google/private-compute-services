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

package com.google.android.`as`.oss.delegatedui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import androidx.core.graphics.drawable.toBitmap
import com.google.protobuf.ByteString

/** Allows CUJs to send and receive [Bitmap]s in gRPC responses as a serialized proto field. */
object SerializableBitmap {

  /** Converts a [Bitmap] to a [ByteString]. */
  fun Bitmap.serializeToByteString(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100,
  ): ByteString {
    val output = ByteString.newOutput()
    compress(format, quality, output)
    return output.toByteString()
  }

  /** Converts a [Icon] to a [ByteString]. */
  fun Icon.serializeToByteString(
    context: Context,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100,
  ): ByteString? {
    return runCatching { loadDrawable(context)?.toBitmap()?.serializeToByteString(format, quality) }
      .getOrNull()
  }

  /** Converts a [ByteString] to a [Bitmap]. */
  fun ByteString.deserializeToBitmap(): Bitmap? =
    runCatching { BitmapFactory.decodeStream(newInput()) }.getOrNull()
}
