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

package(default_visibility = [
    "//visibility:public",
])

android_library(
    name = "proxy_token_provider",
    srcs = ["AuthenticatingProxyTokenProvider.kt"],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/util:result_ext",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
    ],
)

android_library(
    name = "proxy_token_provider_module",
    srcs = [
        "AuthenticatingProxyTokenProviderModule.kt",
    ],
    deps = [
        ":proxy_token_provider",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:annotations",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
    ],
)

android_library(
    name = "aratea_token_provider",
    srcs = ["AuthenticatingArateaTokenProvider.kt"],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/util:result_ext",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
    ],
)

android_library(
    name = "aratea_token_provider_module",
    srcs = [
        "AuthenticatingArateaTokenProviderModule.kt",
    ],
    deps = [
        ":aratea_token_provider",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:annotations",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
    ],
)

android_library(
    name = "cacheable_aratea_token_provider",
    srcs = ["AuthenticatingCacheableArateaTokenProvider.kt"],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/util:result_ext",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
    ],
)

android_library(
    name = "cacheable_aratea_token_provider_module",
    srcs = [
        "AuthenticatingCacheableArateaTokenProviderModule.kt",
    ],
    deps = [
        ":cacheable_aratea_token_provider",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:annotations",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)
