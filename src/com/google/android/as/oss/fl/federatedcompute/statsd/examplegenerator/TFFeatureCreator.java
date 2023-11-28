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

package com.google.android.as.oss.fl.federatedcompute.statsd.examplegenerator;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.protobuf.ByteString;
import java.util.Collection;
import java.util.List;
import org.tensorflow.example.BytesList;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FloatList;
import org.tensorflow.example.Int64List;

/** Utilities for creating TF features. */
public final class TFFeatureCreator {
  /** Returns an {@link Int64List} feature with the specified value. */
  public static final Feature int64List(long value) {
    return Feature.newBuilder()
        .setInt64List(Int64List.newBuilder().addValue(value).build())
        .build();
  }

  /** Returns an {@link Int64List} feature with the specified values. */
  public static final Feature int64List(Collection<Long> values) {
    return Feature.newBuilder()
        .setInt64List(Int64List.newBuilder().addAllValue(values).build())
        .build();
  }

  /** Returns an {@link Int64List} feature with the specified value. */
  public static final Feature int64List(boolean value) {
    return Feature.newBuilder()
        .setInt64List(Int64List.newBuilder().addValue(value ? 1L : 0L).build())
        .build();
  }

  /** Returns an {@link Int64List} feature with the specified value. */
  public static final Feature int64List(int value) {
    return Feature.newBuilder()
        .setInt64List(Int64List.newBuilder().addValue((long) value).build())
        .build();
  }

  /** Returns a {@link FloatList} feature with the specified value. */
  public static final Feature floatList(float value) {
    return Feature.newBuilder()
        .setFloatList(FloatList.newBuilder().addValue(value).build())
        .build();
  }

  /** Returns an {@link FloatList} feature with the specified values. */
  public static final Feature floatList(Collection<Float> values) {
    return Feature.newBuilder()
        .setFloatList(FloatList.newBuilder().addAllValue(values).build())
        .build();
  }

  /** Returns an {@link FloatList} feature with the specified value. */
  public static final Feature doubleList(double value) {
    return Feature.newBuilder()
        .setFloatList(FloatList.newBuilder().addValue((float) value).build())
        .build();
  }

  /** Returns an {@link FloatList} feature with the specified values. */
  public static final Feature doubleList(Collection<Double> values) {
    return Feature.newBuilder()
        .setFloatList(
            FloatList.newBuilder()
                .addAllValue(values.stream().map(Double::floatValue).collect(toImmutableList()))
                .build())
        .build();
  }

  /** Returns a {@link BytesList} feature with the specified value. */
  public static final Feature bytesList(String value) {
    return Feature.newBuilder()
        .setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8(value)).build())
        .build();
  }

  /** Returns a {@link BytesList} feature with the specified value. */
  public static final Feature bytesList(List<String> values) {
    return Feature.newBuilder()
        .setBytesList(
            BytesList.newBuilder()
                .addAllValue(
                    values.stream().map(ByteString::copyFromUtf8).collect(toImmutableList()))
                .build())
        .build();
  }

  private TFFeatureCreator() {}
}
