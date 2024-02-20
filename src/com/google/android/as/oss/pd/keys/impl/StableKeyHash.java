/*
 * Copyright 2023 Google LLC
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

import com.google.crypto.tink.AccessesPartialKey;
import com.google.crypto.tink.Key;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.EciesPublicKey;
import com.google.crypto.tink.hybrid.HpkePublicKey;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.security.spec.ECPoint;

/** StableKeyHash provides stable byte sequence for hashing. */
public class StableKeyHash {

  private StableKeyHash() {}

  /** Derives a stable byte sequence to be used for hashing. */
  @AccessesPartialKey
  public static ByteString getHashInput(KeysetHandle publicKeysetHandle) {
    if (publicKeysetHandle.size() != 1) {
      throw new IllegalArgumentException("Expected exactly 1 key.");
    }

    Key key = publicKeysetHandle.getPrimary().getKey();
    if (key instanceof HpkePublicKey) {
      return ByteString.copyFrom(((HpkePublicKey) key).getPublicKeyBytes().toByteArray());
    } else if (key instanceof EciesPublicKey) {
      return eciesAeadHkdfPublicKeyToBytes((EciesPublicKey) key);
    } else {
      throw new IllegalArgumentException("Unexpected key type: " + key.getClass());
    }
  }

  @AccessesPartialKey
  private static ByteString eciesAeadHkdfPublicKeyToBytes(EciesPublicKey eciesPublicKey) {
    ByteString.Output output = ByteString.newOutput();
    try {
      // Format as RFC 8422/ANSI.X9-62.2005 specified key
      output.write(0x04);
      ECPoint nistCurvePoint = eciesPublicKey.getNistCurvePoint();
      if (nistCurvePoint == null) {
        throw new IllegalArgumentException("Invalid eciesPublicKey. ECPoint is null.");
      }
      output.write(nistCurvePoint.getAffineX().toByteArray());
      output.write(nistCurvePoint.getAffineY().toByteArray());
    } catch (IOException e) {
      throw new AssertionError("Unexpected failure building ByteString.", e);
    }

    return output.toByteString();
  }
}
