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

package com.google.android.as.oss.fl.federatedcompute.init;

import com.google.android.as.oss.fl.Annotations.CustomTFLibName;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * A module that provides name of custom native tensorflow library.
 *
 * <p>The custom native library contains a selection of regular TensorFlow and custom ops that are
 * necessary to fulfill the device personalization computations.
 */
@Module
@InstallIn(SingletonComponent.class)
abstract class CustomLibModule {

  private static final String CUSTOM_TF_LIB = "pcs_tensorflow_jni";

  @Provides
  @CustomTFLibName
  static String provideCustomTFLib() {
    return CUSTOM_TF_LIB;
  }
}
