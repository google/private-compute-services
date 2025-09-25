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

import static com.google.common.base.Strings.isNullOrEmpty;

import android.util.Base64;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Simple base implementation of a {@link FlagManager}. */
public abstract class AbstractFlagManager implements FlagManager {
  private static final class LazyLogger {
    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  }

  private static final Splitter STRING_LIST_SPLITTER =
      Splitter.on(",").trimResults().omitEmptyStrings();

  @Override
  public Boolean get(BooleanFlag flag, Boolean defaultOverride) {
    final String property = getProperty(flag.name());
    if (property == null) {
      return defaultOverride;
    }

    return switch (property) {
      case "true" -> true;
      case "false" -> false;
      default -> {
        logReadWarning(
            flag.name(),
            "boolean",
            new IllegalStateException("Value " + property + " is not a valid boolean"));
        yield defaultOverride;
      }
    };
  }

  @Override
  public Integer get(IntegerFlag flag, Integer defaultOverride) {
    final String property = getProperty(flag.name());
    if (property == null) {
      return defaultOverride;
    }
    try {
      return Integer.parseInt(property);
    } catch (NumberFormatException e) {
      logReadWarning(flag.name(), "integer", e);
      return defaultOverride;
    }
  }

  @Override
  public Long get(LongFlag flag, Long defaultOverride) {
    final String property = getProperty(flag.name());
    if (property == null) {
      return defaultOverride;
    }
    try {
      return Long.parseLong(property);
    } catch (NumberFormatException e) {
      logReadWarning(flag.name(), "long", e);
      return defaultOverride;
    }
  }

  @Override
  public Float get(FloatFlag flag, Float defaultOverride) {
    final String property = getProperty(flag.name());
    if (property == null) {
      return defaultOverride;
    }
    try {
      return Float.parseFloat(property);
    } catch (NumberFormatException e) {
      logReadWarning(flag.name(), "float", e);
      return defaultOverride;
    }
  }

  @Override
  public String get(StringFlag flag, String defaultOverride) {
    final String property = getProperty(flag.name());
    return property == null ? defaultOverride : property;
  }

  @Override
  public <T extends Enum<T>> T get(EnumFlag<T> flag, T defaultOverride) {
    final String property = getProperty(flag.name());
    if (property == null) {
      return defaultOverride;
    }

    T value;
    try {
      value = Enum.valueOf(flag.type(), property);
    } catch (IllegalArgumentException e) {
      LazyLogger.logger
          .atWarning()
          .withCause(e)
          .log(
              "Received flag value '%s' is not in the values list for enum '%s'. Falling back to"
                  + " defaults.",
              property, flag.type().getName());
      return defaultOverride;
    }

    return value;
  }

  @Override
  public ImmutableList<String> get(StringListFlag flag, ImmutableList<String> defaultOverride) {
    final String property = getProperty(flag.name());
    if (property == null) {
      return defaultOverride;
    }
    return ImmutableList.copyOf(STRING_LIST_SPLITTER.split(property));
  }

  @Override
  public <ResultT extends MessageLite> ResultT get(
      ProtoFlag<ResultT> flag, ResultT defaultOverride) {
    try {
      return getProtoFlag(flag.name(), defaultOverride, flag.merge());
    } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
      LazyLogger.logger
          .atSevere()
          .withCause(e)
          .log("Failed to parse proto. Flag name = %s.", flag.name());
      return defaultOverride;
    }
  }

  /** Returns the raw string value for a given flag name. */
  protected abstract @Nullable String getProperty(String name);

  private static void logReadWarning(String name, String type, Throwable t) {
    LazyLogger.logger
        .atWarning()
        .withCause(t)
        .log("Failed to get a property for name %s with type %s, returning safe value", name, type);
  }

  @SuppressWarnings("unchecked") // Guaranteed by runtime.
  private <ResultT extends MessageLite> ResultT getProtoFlag(
      String flagName, ResultT defaultOverride, boolean merge)
      throws InvalidProtocolBufferException {
    String base64Proto = getProperty(flagName);
    if (isNullOrEmpty(base64Proto)) {
      return defaultOverride;
    }
    byte[] decodedProto = Base64.decode(base64Proto, Base64.DEFAULT);
    if (merge) {
      return (ResultT)
          defaultOverride.toBuilder()
              .mergeFrom(decodedProto, ExtensionRegistryLite.getEmptyRegistry())
              .build();
    }
    return (ResultT)
        defaultOverride
            .getParserForType()
            .parseFrom(decodedProto, ExtensionRegistryLite.getEmptyRegistry());
  }
}
