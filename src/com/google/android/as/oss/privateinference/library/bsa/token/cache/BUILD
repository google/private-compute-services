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
    name = "cache_control_plane",
    srcs = ["BsaTokenCacheControlPlane.kt"],
)

android_library(
    name = "flag",
    srcs = ["TokenCacheFlag.kt"],
)

android_library(
    name = "token_pool",
    srcs = ["TokenPool.kt"],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
    ],
)

android_library(
    name = "memory_token_pool",
    srcs = ["MemoryTokenPool.kt"],
    deps = [
        ":token_pool",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
    ],
)

android_library(
    name = "caching_token_provider",
    srcs = ["CachingBsaTokenProvider.kt"],
    deps = [
        ":cache_control_plane",
        ":token_pool",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/util:result_ext",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:androidx_annotation_annotation",
    ],
)

android_library(
    name = "cache_refresh_worker",
    srcs = ["BsaTokenCacheRefreshWorker.kt"],
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        ":cache_control_plane",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//third_party/java/androidx/work",
        "@maven//:com_google_flogger_google_extensions",
    ],
)

android_library(
    name = "worker_module",
    srcs = ["WorkerModule.kt"],
    deps = [
        ":cache_control_plane",
        ":cache_refresh_worker",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache:flag",
        "//third_party/java/androidx/work",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "optionals_module",
    srcs = ["OptionalCachingTokenProviderModule.kt"],
    deps = [
        ":cache_control_plane",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "caching_module",
    srcs = ["CachingTokenProviderModule.kt"],
    deps = [
        ":cache_control_plane",
        ":caching_token_provider",
        ":memory_token_pool",
        ":token_pool",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache/db",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache/db:db_token_pool",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/crypto",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
