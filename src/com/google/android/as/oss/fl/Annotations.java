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

package com.google.android.as.oss.fl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

/** Interfaces for setting up ExampleStoreService in PCS. */
public final class Annotations {

  /** Annotation for providing ExampleStoreService info for PCS clients. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ExampleStoreClientsInfo {}

  /** Annotation for providing ResultHandlingService info for PCS clients. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ResultHandlingClientsInfo {}

  /** Annotation for providing Android System Intelligence package name. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface AsiPackageName {}

  /** Annotation for providing Google Play Protect Service package name. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface GppsPackageName {}

  /**
   * Annotation for providing custom TensorFlow library name.
   *
   * <p>The custom native library contains a selection of regular TensorFlow and custom ops that are
   * necessary to fulfill the device personalization computations.
   */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface CustomTFLibName {}

  private Annotations() {}
}
