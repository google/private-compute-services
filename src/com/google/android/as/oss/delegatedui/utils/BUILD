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
        "//visibility:public",
    ],
)

android_library(
    name = "parcelable_key",
    srcs = ["ParcelableKey.kt"],
    deps = [
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:io_grpc_grpc_context",
    ],
)

android_library(
    name = "server_interceptor",
    srcs = ["ParcelableOverMetadataServerInterceptor.kt"],
    deps = [
        ":parcelable_key",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_context",
    ],
)

android_library(
    name = "parcelable_over_rpc",
    srcs = ["ParcelableOverRpcUtils.kt"],
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        ":parcelable_key",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_context",
        "@maven//:io_grpc_grpc_stub",
    ],
)

android_library(
    name = "parcelable_over_rpc_base_module",
    srcs = ["ParcelableOverRpcUtilsBaseModule.kt"],
    tags = ["keep_dep"],
    deps = [
        ":parcelable_over_rpc",
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "parcelable_over_rpc_hilt_module",
    srcs = ["ParcelableOverRpcUtilsHiltModule.kt"],
    tags = ["keep_dep"],
    deps = [
        ":parcelable_over_rpc_base_module",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "parcelable_over_rpc_testing",
    srcs = ["FakeParcelableOverRpcUtils.kt"],
    tags = ["keep_dep"],
    deps = [
        ":parcelable_key",
        ":parcelable_over_rpc",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_context",
        "@maven//:io_grpc_grpc_stub",
    ],
)

android_library(
    name = "response_with_parcelables",
    srcs = ["ResponseWithParcelables.kt"],
    deps = [
        "//src/com/google/android/as/oss/delegatedui/utils:parcelable_over_rpc",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

android_library(
    name = "serializable_bitmap",
    srcs = ["SerializableBitmap.kt"],
    deps = [
        "//third_party/java/androidx/core/ktx",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

android_library(
    name = "tintable_icon",
    srcs = ["TintableIcon.kt"],
    deps = [
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
    ],
)
