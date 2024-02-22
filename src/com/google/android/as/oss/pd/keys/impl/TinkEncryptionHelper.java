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

import com.google.android.as.oss.pd.keys.EncryptionHelper;
import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.TinkProtoKeysetFormat;
import java.io.IOException;
import java.security.GeneralSecurityException;

/** An {@link EncryptionHelper} implemented using Tink open source library. */
class TinkEncryptionHelper implements EncryptionHelper {

  private final MasterKeyProvider masterKeyProvider;
  private final KeysetHandle handle;
  private final boolean hasPrivateKey;

  TinkEncryptionHelper(
      MasterKeyProvider masterKeyProvider, KeysetHandle handle, boolean hasPrivateKey) {
    this.masterKeyProvider = masterKeyProvider;
    this.handle = handle;
    this.hasPrivateKey = hasPrivateKey;
  }

  @Override
  public boolean hasPrivateKey() {
    return hasPrivateKey;
  }

  @Override
  public byte[] decrypt(byte[] encryptedData, byte[] associatedData)
      throws GeneralSecurityException {
    if (!hasPrivateKey) {
      throw new GeneralSecurityException("cannot decrypt without a private key");
    }
    HybridDecrypt decrypt = handle.getPrimitive(HybridDecrypt.class);
    return decrypt.decrypt(encryptedData, associatedData);
  }

  @Override
  public byte[] encrypt(byte[] plainData, byte[] associatedData) throws GeneralSecurityException {
    HybridEncrypt encrypt = getPublicKeysetHandle().getPrimitive(HybridEncrypt.class);
    return encrypt.encrypt(plainData, associatedData);
  }

  @Override
  public byte[] publicKey() throws GeneralSecurityException, IOException {
    return TinkProtoKeysetFormat.serializeKeysetWithoutSecret(getPublicKeysetHandle());
  }

  @Override
  public byte[] toEncryptedKeySet() throws GeneralSecurityException, IOException {
    return TinkProtoKeysetFormat.serializeEncryptedKeyset(
        handle, masterKeyProvider.readOrGenerateMasterKey(), new byte[] {});
  }

  @Override
  public String publicKeyHashForLogging() throws GeneralSecurityException, IOException {
    return StableKeyHash.hashKey(getPublicKeysetHandle()).toString();
  }

  private KeysetHandle getPublicKeysetHandle() throws GeneralSecurityException {
    return hasPrivateKey ? handle.getPublicKeysetHandle() : handle;
  }
}
