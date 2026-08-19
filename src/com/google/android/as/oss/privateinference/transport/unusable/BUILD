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

package(
    default_visibility = [
        "//javatests/com/google/android/apps/miphone/pcs:__subpackages__",
        "//src/com/google/android/as/oss:__subpackages__",
    ],
)

android_library(
    name = "unusable",
    srcs = [
        "UnusableManagedChannel.kt",
        "UnusableManagedChannelFactory.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_kt_proto_lite",
        "//src/com/google/android/as/oss/privateinference/transport:managed_channel_factory",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "unusable_transport_module",
    srcs = ["UnusableTransportModule.kt"],
    deps = [
        ":unusable",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//src/com/google/android/as/oss/privateinference/transport:managed_channel_factory",
        "//src/com/google/android/as/oss/privateinference/transport:phosphor_factory",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)
