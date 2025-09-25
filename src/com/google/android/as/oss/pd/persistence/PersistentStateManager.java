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

package com.google.android.as.oss.pd.persistence;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;

/** A manager for handling the I/O of the state persisted for each client of protected download. */
public interface PersistentStateManager {

  /** Special client ID to persist state for the shared VM instance. */
  String VM_CLIENT_ID = "VM_CLIENT";

  /**
   * Returns the state persisted for the given client, or {@link Optional#empty()} if no state is
   * persisted for this client.
   *
   * @return A {@link ListenableFuture} with the read state, empty or with an error in case of a
   *     failure to read the persisted value.
   */
  ListenableFuture<Optional<ClientPersistentState>> readState(String clientId);

  /**
   * Stores the state for the given client.
   *
   * @return A {@link ListenableFuture} indicating when the operation has finished, or an error if
   *     the operation has failed.
   */
  ListenableFuture<Void> writeState(String clientId, ClientPersistentState state);
}
