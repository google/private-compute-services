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
    name = "crypto",
    srcs = [
        "BsaTokenCipher.kt",
        "EncryptedBsaTokenBytes.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//third_party/java/okio:okio_android",
    ],
)

android_library(
    name = "tink_crypto_impl",
    srcs = [
        "TinkBsaTokenCipher.kt",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        ":crypto",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//third_party/java/androidx/core/ktx",
        "//third_party/java/okio:okio_android",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines_guava",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_crypto_tink_tink_android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "crypto_module",
    srcs = [
        "BsaTokenCipherModule.kt",
    ],
    deps = [
        ":crypto",
        ":tink_crypto_impl",
        "//src/com/google/android/as/oss/common/initializer",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
