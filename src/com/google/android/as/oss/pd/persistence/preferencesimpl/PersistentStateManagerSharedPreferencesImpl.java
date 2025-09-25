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

package com.google.android.as.oss.pd.persistence.preferencesimpl;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.ProtectedDownloadExecutorQualifier;
import com.google.android.as.oss.pd.persistence.ClientPersistentState;
import com.google.android.as.oss.pd.persistence.PersistentStateManager;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A {@link PersistentStateManager} that persists the data into a private shared preferences file
 * accessible only to the host app.
 */
@Singleton
class PersistentStateManagerSharedPreferencesImpl implements PersistentStateManager {

  @VisibleForTesting
  static final String PROTECTED_DOWNLOAD_CLIENT_STATE_FILE = "protected_download_persistent_state";

  private final Context context;
  private final ListeningExecutorService executorService;

  @Inject
  PersistentStateManagerSharedPreferencesImpl(
      @ApplicationContext Context context,
      @ProtectedDownloadExecutorQualifier ListeningExecutorService executorService) {
    this.context = context;
    this.executorService = executorService;
  }

  @Override
  public ListenableFuture<Optional<ClientPersistentState>> readState(String clientId) {
    return executorService.submit(
        () ->
            toClientState(
                Optional.ofNullable(
                    getSharedPreferences().getString(toClientStateKey(clientId), null))));
  }

  @Override
  public ListenableFuture<Void> writeState(String clientId, ClientPersistentState state) {
    return executorService.submit(
        () -> {
          getSharedPreferences()
              .edit()
              .putString(toClientStateKey(clientId), toClientStateValue(state))
              .apply();
          return null;
        });
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(PROTECTED_DOWNLOAD_CLIENT_STATE_FILE, Context.MODE_PRIVATE);
  }

  @VisibleForTesting
  static String toClientStateKey(String clientId) {
    return BaseEncoding.base16().encode(clientId.getBytes(UTF_8));
  }

  private static String toClientStateValue(ClientPersistentState state) {
    return BaseEncoding.base16().encode(state.toByteArray());
  }

  private static Optional<ClientPersistentState> toClientState(Optional<String> clientStateValue)
      throws InvalidProtocolBufferException {
    // Can't use clientStateValue.map(...) since exception is thrown.
    return clientStateValue.isPresent()
        ? Optional.of(
            ClientPersistentState.parseFrom(
                BaseEncoding.base16().decode(clientStateValue.get()),
                ExtensionRegistryLite.getGeneratedRegistry()))
        : Optional.empty();
  }
}
