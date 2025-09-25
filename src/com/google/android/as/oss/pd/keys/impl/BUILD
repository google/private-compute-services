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

load("@bazel_rules_android//android:rules.bzl", "android_library", "java_library_with_nullness_check")

package(default_visibility = [
    "//visibility:public",
])

android_library(
    name = "impl",
    srcs = [
        "MasterKeyProvider.java",
        "TinkEncryptionHelper.java",
        "TinkEncryptionHelperFactory.java",
    ],
    deps = [
        ":stable_key_hash",
        "//src/com/google/android/as/oss/pd/keys",
        "@maven//:com_google_crypto_tink_tink_android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "encryption_module",
    srcs = [
        "TinkEncryptionHelperModule.java",
    ],
    deps = [
        ":impl",
        "//src/com/google/android/as/oss/pd/keys",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "android_keystore_module",
    srcs = [
        "AndroidKeyStoreMasterKeyProviderModule.java",
    ],
    deps = [
        ":impl",
        "@maven//:com_google_crypto_tink_tink_android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "stable_key_hash",
    srcs = ["StableKeyHash.java"],
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "@maven//:com_google_crypto_tink_tink_android",
        "@maven//:com_google_guava_guava",
    ],
)

java_library_with_nullness_check(
    name = "stable_key_hash_server",
    srcs = ["StableKeyHash.java"],
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//third_party/java/tink:accesses_partial_key",
        "//third_party/java/tink:hybrid",
        "//third_party/java/tink:tink_core",
        "@maven//:com_google_guava_guava",
    ],
)
