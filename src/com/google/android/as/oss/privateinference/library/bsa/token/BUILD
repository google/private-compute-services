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

load("@bazel_rules_android//android:rules.bzl", "android_library", "kt_jvm_library_with_nullness_check")

package(default_visibility = [
    "//java/com/google/android/apps/internal/mdi/privatearatea:__subpackages__",
    "//visibility:public",
])

android_library(
    name = "token",
    srcs = [
        "BsaToken.kt",
        "BsaTokenBytes.kt",
    ],
    deps = [
        "//third_party/java/okio",
        "@maven//:javax_inject_javax_inject",  # unuseddeps: keep
    ],
)

kt_jvm_library_with_nullness_check(
    name = "token_jvm",
    srcs = [
        "BsaToken.kt",
        "BsaTokenBytes.kt",
    ],
    deps = [
        "//third_party/java/okio",
        "@maven//:javax_inject_javax_inject",  # unuseddeps: keep
    ],
)

android_library(
    name = "annotations",
    srcs = [
        "BsaAnnotations.kt",
    ],
    deps = [
        "@maven//:javax_inject_javax_inject",  # unuseddeps: keep
    ],
)

android_library(
    name = "token_provider",
    srcs = [
        "BsaTokenParams.kt",
        "BsaTokenProvider.kt",
    ],
    deps = [
        ":token",
        "//third_party/java/okio",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines_guava-android",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "configurable_token_provider",
    srcs = [
        "ConfigurableBsaTokenProvider.kt",
    ],
    deps = [
        ":token",
        ":token_provider",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache:cache_control_plane",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache:flag",
        "//src/com/google/android/as/oss/privateinference/logging",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_flogger_google_extensions",
    ],
)

android_library(
    name = "noop_token_provider_module",
    srcs = ["NoopTokenProviderModule.kt"],
    visibility = [
        "//java/com/google/android/libraries/mdi/privatearatea:__subpackages__",
        "//visibility:public",
    ],
    deps = [
        ":token",
        ":token_provider",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/privateinference/library/bsa/impl:blind_sign_auth_impl",  # unuseddeps: keep
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "module",
    srcs = [
        "BsaTokenProviderModule.kt",
    ],
    deps = [
        ":annotations",
        ":configurable_token_provider",
        ":token",
        ":token_provider",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/logging",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "token_modules",
    exports = [
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/auth:aratea_token_provider_module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/auth:cacheable_aratea_token_provider_module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/auth:proxy_token_provider_module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache:caching_module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache:optionals_module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache:worker_module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache/db:module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/crypto:crypto_module",
    ],
)
