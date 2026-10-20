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

package com.google.android.as.oss.common.config;

import android.util.Base64;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.MessageLite;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A fake implementation of {@link FlagManager}. Exposes setters and stores all flag values in
 * memory.
 */
public final class FakeFlagManager extends AbstractFlagManager {
  private final ConcurrentHashMap<String, String> values = new ConcurrentHashMap<>();
  private final MulticastListenable<FlagListener> listenable = MulticastListenable.create();

  public static FakeFlagManager create() {
    return new FakeFlagManager();
  }

  public void clearValues() {
    values.clear();
  }

  @Override
  public Listenable<FlagListener> listenable() {
    return listenable;
  }

  @Override
  public @Nullable String getProperty(String name) {
    return values.get(name);
  }

  @Deprecated
  public void setBoolean(String name, Boolean value) {
    setValue(name, value.toString());
  }

  public void set(BooleanFlag flag, Boolean value) {
    setValue(flag.name(), value.toString());
  }

  public void set(IntegerFlag flag, Integer value) {
    setValue(flag.name(), value.toString());
  }

  public void set(LongFlag flag, Long value) {
    setValue(flag.name(), value.toString());
  }

  public void set(FloatFlag flag, float value) {
    setValue(flag.name(), Float.toString(value));
  }

  public void set(StringFlag flag, String value) {
    setValue(flag.name(), value);
  }

  public void set(StringListFlag flag, ImmutableList<String> value) {
    setValue(flag.name(), String.join(",", value));
  }

  public <T extends Enum<T>> void set(EnumFlag<T> flag, T value) {
    setValue(flag.name(), value.toString());
  }

  public <T extends MessageLite> void set(ProtoFlag<T> flag, T value) {
    setValue(flag.name(), Base64.encodeToString(value.toByteArray(), Base64.DEFAULT));
  }

  public void notifyFlagsChanged(ImmutableList<String> flagNames) {
    listenable.notify(listener -> listener.onUpdated(flagNames));
  }

  private void setValue(String name, String value) {
    values.put(name, value);
    listenable.notify(listener -> listener.onUpdated(ImmutableList.of(name)));
  }

  private FakeFlagManager() {}
}
