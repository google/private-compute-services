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

package com.google.android.as.oss.pd.keys.impl;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.crypto.tink.AccessesPartialKey;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.EciesPublicKey;
import com.google.crypto.tink.hybrid.HpkePublicKey;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.spec.ECPoint;

/** StableKeyHash provides stable hash for public key. */
public class StableKeyHash {

  private StableKeyHash() {}

  /** Calculates a stable hash of public key. */
  public static HashCode hashKey(KeysetHandle publicKeysetHandle) {
    ByteBuffer hashInput = ByteBuffer.allocate(128);
    putKeyset(hashInput, publicKeysetHandle);
    hashInput.flip(); // Flip to read.
    return Hashing.sha256().hashBytes(hashInput);
  }

  /** Derives a stable byte sequence to be used for hashing. */
  @AccessesPartialKey
  static void putKeyset(ByteBuffer hashInput, KeysetHandle publicKeysetHandle) {
    if (publicKeysetHandle.size() != 1) {
      throw new IllegalArgumentException("Expected exactly 1 key.");
    }
    // Format as RFC 8422/ANSI.X9-62.2005 specified key
    hashInput.put((byte) 0x04);

    Key key = publicKeysetHandle.getPrimary().getKey();
    if (key instanceof HpkePublicKey) {
      hashInput.put(((HpkePublicKey) key).getPublicKeyBytes().toByteArray());
    } else if (key instanceof EciesPublicKey) {
      putEciesAeadHkdfPublicKey(hashInput, (EciesPublicKey) key);
    } else {
      throw new IllegalArgumentException("Unexpected key type: " + key.getClass());
    }
  }

  @AccessesPartialKey
  private static void putEciesAeadHkdfPublicKey(
      ByteBuffer hashInput, EciesPublicKey eciesPublicKey) {
    ECPoint nistCurvePoint = eciesPublicKey.getNistCurvePoint();
    if (nistCurvePoint == null) {
      throw new IllegalArgumentException("Invalid eciesPublicKey. ECPoint is null.");
    }
    putBigInteger(hashInput, nistCurvePoint.getAffineX());
    putBigInteger(hashInput, nistCurvePoint.getAffineY());
  }

  private static void putBigInteger(ByteBuffer hashInput, BigInteger bigInteger) {
    byte[] buffer = bigInteger.toByteArray();
    // BigInteger may produce output with additional leading zero. Strip leading 0
    // to produce stable hash.
    if (buffer.length > 0 && buffer[0] == 0) {
      hashInput.put(buffer, 1, buffer.length - 1);
    } else {
      hashInput.put(buffer);
    }
  }
}
