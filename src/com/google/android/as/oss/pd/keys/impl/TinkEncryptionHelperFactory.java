/*
 * Copyright 2021 Google LLC
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

import com.google.android.as.oss.pd.keys.EncryptionHelperFactory;
import com.google.crypto.tink.BinaryKeysetReader;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.hybrid.HybridConfig;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * An {@link EncryptionHelperFactory} implemented using Tink open-source library. Key sets are
 * serialized / deserialized in encrypted form using a key provided by {@link MasterKeyProvider}.
 */
@Singleton
final class TinkEncryptionHelperFactory implements EncryptionHelperFactory {

  private static final String KEY_TEMPLATE_STRING = "ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM";

  private final MasterKeyProvider masterKeyProvider;
  private volatile boolean initialized;
  private KeyTemplate keyTemplate;

  @Inject
  TinkEncryptionHelperFactory(MasterKeyProvider masterKeyProvider) {
    this.masterKeyProvider = masterKeyProvider;
  }

  @Override
  public TinkEncryptionHelper generateEncryptedKeySet() throws GeneralSecurityException {
    initializeIfNeeded();
    return new TinkEncryptionHelper(
        masterKeyProvider, KeysetHandle.generateNew(keyTemplate), /* hasPrivateKey= */ true);
  }

  @Override
  public TinkEncryptionHelper createFromEncryptedKeySet(byte[] encryptedKeyset)
      throws GeneralSecurityException, IOException {
    initializeIfNeeded();
    return new TinkEncryptionHelper(
        masterKeyProvider,
        KeysetHandle.read(
            BinaryKeysetReader.withBytes(encryptedKeyset),
            masterKeyProvider.readOrGenerateMasterKey()),
        /* hasPrivateKey= */ true);
  }

  @Override
  public TinkEncryptionHelper createFromPublicKey(byte[] publicKey)
      throws GeneralSecurityException, IOException {
    initializeIfNeeded();
    return new TinkEncryptionHelper(
        masterKeyProvider, KeysetHandle.readNoSecret(publicKey), /* hasPrivateKey= */ false);
  }

  private void initializeIfNeeded() throws GeneralSecurityException {
    if (!initialized) {
      // There is no harm if this code is called multiple times in case on concurrent calls, so
      // the initialized check is merely a performance enhancement not to call it every time, but
      // there is no need to protect against concurrent calls.
      HybridConfig.register();
      keyTemplate = KeyTemplates.get(KEY_TEMPLATE_STRING);
      initialized = true;
    }
  }
}
