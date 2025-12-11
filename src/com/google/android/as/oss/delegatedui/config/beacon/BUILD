# Copyright 2025 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

load("@bazel_rules_android//android:rules.bzl", "android_library")
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl", "kt_jvm_lite_proto_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "beacon",
    srcs = [
        "BeaconConfig.kt",
        "BeaconConfigReader.kt",
        "BeaconFlags.kt",
    ],
    deps = [
        ":beacon_configs_kt_proto_lite",
        "//src/com/google/android/as/oss/common/config",
    ],
)

android_library(
    name = "module",
    srcs = [
        "BeaconConfigModule.kt",
    ],
    deps = [
        ":beacon",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

proto_library(
    name = "beacon_configs_proto",
    srcs = ["beacon_configs.proto"],
)

kt_jvm_lite_proto_library(
    name = "beacon_configs_kt_proto_lite",
    deps = [":beacon_configs_proto"],
)

java_lite_proto_library(
    name = "beacon_configs_java_proto_lite",
    deps = [":beacon_configs_proto"],
)
