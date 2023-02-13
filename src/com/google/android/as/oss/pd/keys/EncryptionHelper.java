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

package com.google.android.as.oss.pd.keys;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * A utility for handling the encryption/decryption of data, assuming the keys, encryption
 * parameters and the associated data are already known.
 */
public interface EncryptionHelper {

  /** Encrypts the given data using the known key and parameters. */
  byte[] encrypt(byte[] plainData, byte[] associatedData) throws GeneralSecurityException;

  /**
   * Decrypts the given encrypted data using the known key and parameters.
   *
   * @throws GeneralSecurityException if this instance does not contain a private key or the
   *     associated data is invalid.
   */
  byte[] decrypt(byte[] encryptedData, byte[] associatedData) throws GeneralSecurityException;

  /** Returns the public key of the keyset in this class. */
  byte[] publicKey() throws GeneralSecurityException, IOException;

  /** Serializes the keys used for encryption/decryption in an encrypted form. */
  byte[] toEncryptedKeySet() throws GeneralSecurityException, IOException;
}
