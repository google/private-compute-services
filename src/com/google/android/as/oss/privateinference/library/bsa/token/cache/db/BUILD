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
    name = "db",
    srcs = [
        "BsaTokenDao.kt",
        "BsaTokenDatabase.kt",
        "BsaTokenEntity.kt",
        "Converters.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache:token_pool",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/crypto",
        "//third_party/java/okio:okio_android",
        "@maven//:androidx_room_room_common",
        "@maven//:androidx_room_room_runtime",
    ],
)

android_library(
    name = "db_token_pool",
    srcs = ["DatabaseTokenPool.kt"],
    deps = [
        ":db",
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/cache:token_pool",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token/crypto",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
    ],
)

android_library(
    name = "module",
    srcs = ["DatabaseModule.kt"],
    deps = [
        ":db",
        "@maven//:androidx_room_room_common",
        "@maven//:androidx_room_room_runtime",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
