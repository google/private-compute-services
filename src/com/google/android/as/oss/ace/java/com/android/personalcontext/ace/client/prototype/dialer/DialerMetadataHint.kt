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

package com.android.personalcontext.ace.client.prototype.dialer

import android.os.Bundle
import android.util.Log
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DialerMetadataHintId
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import java.io.ByteArrayOutputStream

/** A hint for CallEmbedded */
@Suppress("DEPRECATION")
data class DialerMetadataHint(
  val vertical: BusinessVertical = BusinessVertical.UNSPECIFIED,
  val displayGeneralCards: Boolean = true,
  val displayAllCards: Boolean = false,
  val isInferredBusiness: Boolean = false,
) : PrototypeHint(DialerMetadataHintId, this) {

  enum class BusinessVertical {
    UNSPECIFIED,
    FOOD_AND_DRINK,
    SERVICE_PROVIDER,
    SHOPPING,
    HEALTH,
    LODGING,
    TRANSPORT,
  }

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putInt(KEY_VERTICAL, vertical.ordinal)
    bundle.putBoolean(KEY_DISPLAY_GENERAL_CARDS, displayGeneralCards)
    bundle.putBoolean(KEY_DISPLAY_ALL_CARDS, displayAllCards)
    bundle.putBoolean(KEY_IS_INFERRED_BUSINESS, isInferredBusiness)

    // Put legacy Protobuf in case receiver only knows about legacy format
    bundle.putByteArray(
      PixelDialerMetadataKeys.KEY_PIXEL_DIALER_METADATA,
      LegacyProtoWireCompat.toByteArray(this),
    )
  }

  companion object : Creator {
    const val TAG = "DialerMetadataHint"
    const val KEY_VERTICAL = "DialerMetadataHint__vertical"
    const val KEY_DISPLAY_GENERAL_CARDS = "DialerMetadataHint__display_general_cards"
    const val KEY_DISPLAY_ALL_CARDS = "DialerMetadataHint__display_all_cards"
    const val KEY_IS_INFERRED_BUSINESS = "DialerMetadataHint__is_inferred_business"

    override fun create(bundle: Bundle): PrototypeHint {
      Log.i(TAG, "[CallEmbedded] DialerMetadataHint#create init")
      val isPrototypeHintFormat =
        bundle.containsKey(KEY_VERTICAL) ||
          bundle.containsKey(KEY_DISPLAY_GENERAL_CARDS) ||
          bundle.containsKey(KEY_DISPLAY_ALL_CARDS) ||
          bundle.containsKey(KEY_IS_INFERRED_BUSINESS)

      if (isPrototypeHintFormat) {
        val prototypeHint = createFromPrototypeHintFormat(bundle)

        Log.i(
          TAG,
          "[CallEmbedded] DialerMetadataHint#create returning native DialerMetadataHint: $prototypeHint",
        )

        return prototypeHint
      }

      // Fallback to legacy Protobuf format
      val bytes = bundle.getByteArray(PixelDialerMetadataKeys.KEY_PIXEL_DIALER_METADATA)

      if (bytes == null) {
        Log.e(
          TAG,
          "[CallEmbedded] DialerMetadataHint#create failed to find KEY_PIXEL_DIALER_METADATA",
        )
        return DialerMetadataHint()
      }

      return LegacyProtoWireCompat.parseFromBytes(bytes)
    }

    private fun createFromPrototypeHintFormat(bundle: Bundle): DialerMetadataHint {
      val ordinal = bundle.getInt(KEY_VERTICAL, BusinessVertical.UNSPECIFIED.ordinal)
      val vertical =
        if (ordinal in 0 until BusinessVertical.entries.size) {
          BusinessVertical.entries[ordinal]
        } else {
          BusinessVertical.UNSPECIFIED
        }

      return DialerMetadataHint(
        vertical = vertical,
        displayGeneralCards = bundle.getBoolean(KEY_DISPLAY_GENERAL_CARDS, true),
        displayAllCards = bundle.getBoolean(KEY_DISPLAY_ALL_CARDS, false),
        isInferredBusiness = bundle.getBoolean(KEY_IS_INFERRED_BUSINESS, false),
      )
    }
  }
}

/**
 * Serializes and deserializes legacy [PixelDialerMetadata] Protobuf wire format bytes directly to
 * avoid an external proto library dependency in AOSP drops.
 *
 * Wire format tag definitions:
 * - Field 1 (Varint): vertical (enum ordinal)
 * - Field 2 (Varint): display_general_cards (boolean)
 * - Field 3 (Varint): display_all_cards (boolean)
 */
private object LegacyProtoWireCompat {
  const val TAG = "LegacyProtoWireCompat"
  const val FIELD_VERTICAL = 1
  const val FIELD_DISPLAY_GENERAL_CARDS = 2
  const val FIELD_DISPLAY_ALL_CARDS = 3

  /** Parses the legacy [PixelDialerMetadata] Protobuf into a [DialerMetadataHint]. */
  fun parseFromBytes(bytes: ByteArray): DialerMetadataHint {
    Log.i(
      TAG,
      "[CallEmbedded] #parseFromBytes init. If you see this log, it means the sender is using legacy format.",
    )
    // Wrap the raw byte array in a CodedInputStream to decode varints and tags.
    val input = CodedInputStream.newInstance(bytes)

    // Protobuf skips writing fields to the byte array if they are false or 0.
    // We set our starting values here so that if a field isn't found in the
    // bytes below, it defaults correctly.
    var vertical = DialerMetadataHint.BusinessVertical.UNSPECIFIED
    var displayGeneralCards = true
    var displayAllCards = false

    // Protobuf serializes data as a sequential stream of (tag, value) pairs.
    while (!input.isAtEnd) {
      // Read the next tag from the stream. A tag of 0 indicates the end of valid data.
      val tag = input.readTag()
      if (tag == 0) break

      // A Protobuf tag combines both the field number and wire type using bitwise operations:
      // tag = (field_number << 3) | wire_type
      // Here, 'wire type 0' represents a VARINT (used for int32, int64, bool, enum).
      when (tag) {
        // Field 1 (vertical), wire type 0 (VARINT)
        (FIELD_VERTICAL shl 3) or 0 -> {
          // Enums are encoded on the wire as varint integers representing their ordinal.
          val ordinal = input.readEnum()
          val _vertical =
            if (ordinal in 0 until DialerMetadataHint.BusinessVertical.entries.size) {
              DialerMetadataHint.BusinessVertical.entries[ordinal]
            } else {
              DialerMetadataHint.BusinessVertical.UNSPECIFIED
            }
          Log.i(TAG, "[CallEmbedded] #parseFromBytes read vertical: ${_vertical}")
          vertical = _vertical
        }
        // Field 2 (display_general_cards), wire type 0 (VARINT)
        (FIELD_DISPLAY_GENERAL_CARDS shl 3) or 0 -> {
          // Booleans are encoded as varints (0 for false, 1 for true).
          val _displayGeneralCards = input.readBool()
          Log.i(
            TAG,
            "[CallEmbedded] #parseFromBytes read displayGeneralCards: ${_displayGeneralCards}",
          )
          displayGeneralCards = _displayGeneralCards
        }
        // Field 3 (display_all_cards), wire type 0 (VARINT)
        (FIELD_DISPLAY_ALL_CARDS shl 3) or 0 -> {
          val _displayAllCards = input.readBool()
          Log.i(TAG, "[CallEmbedded] #parseFromBytes read displayAllCards: ${_displayAllCards}")
          displayAllCards = _displayAllCards
        }
        // Unknown field or wire type (e.g., if generated by a newer proto definition).
        // Safely skip the field based on its wire type to ensure forward compatibility.
        else -> {
          input.skipField(tag)
        }
      }
    }

    Log.i(
      TAG,
      "[CallEmbedded] #parseFromBytes Converted to: vertical: $vertical, displayGeneralCards: $displayGeneralCards, displayAllCards: $displayAllCards",
    )

    return DialerMetadataHint(
      vertical = vertical,
      displayGeneralCards = displayGeneralCards,
      displayAllCards = displayAllCards,
    )
  }

  /** Parses [DialerMetadataHint] into legacy [PixelDialerMetadata] Protobuf wire format bytes. */
  fun toByteArray(hint: DialerMetadataHint): ByteArray {
    val outputStream = ByteArrayOutputStream()
    val output = CodedOutputStream.newInstance(outputStream)

    output.writeEnum(FIELD_VERTICAL, hint.vertical.ordinal)
    output.writeBool(FIELD_DISPLAY_GENERAL_CARDS, hint.displayGeneralCards)
    output.writeBool(FIELD_DISPLAY_ALL_CARDS, hint.displayAllCards)

    output.flush()
    return outputStream.toByteArray()
  }
}
