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

package com.google.android.as.oss.privateinference.library.oakutil;

import android.content.Context;
import com.google.android.as.oss.privateinference.library.oakutil.proto.VerificationKeys;
import com.google.protobuf.ExtensionRegistryLite;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
abstract class PrivateInferenceVerificationKeysProdModule {

  @Provides
  @Singleton
  static VerificationKeys provideVerificationKeys(@ApplicationContext Context context) {
    try (InputStream inStream =
        context.getResources().openRawResource(R.raw.public_keys_prod_proto)) {
      return VerificationKeys.parseFrom(inStream, ExtensionRegistryLite.getGeneratedRegistry());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read public keys", e);
    }
  }
}
