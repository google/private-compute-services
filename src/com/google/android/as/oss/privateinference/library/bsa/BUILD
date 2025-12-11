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
    "//javatests/com/google/android/apps/miphone/pcs/privateinference/library/bsa:__subpackages__",
    "//src/com/google/android/as/oss/privateinference/library/bsa:__subpackages__",
    "//visibility:public",
])

android_library(
    name = "blind_sign_auth",
    srcs = ["BlindSignAuth.kt"],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "blind_sign_auth_jvm",
    srcs = ["BlindSignAuth.kt"],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_jvm",
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)

android_library(
    name = "blind_sign_auth_module",
    srcs = ["BlindSignAuthModule.kt"],
    deps = [
        ":blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//src/com/google/android/as/oss/privateinference/config/impl:device_info",
        "//src/com/google/android/as/oss/privateinference/library/bsa/impl:android_keystore_attester",
        "//src/com/google/android/as/oss/privateinference/library/bsa/impl:blind_sign_auth_impl",
        "//src/com/google/android/as/oss/privateinference/library/bsa/impl:phosphor_grpc_message_interface",
        "//src/com/google/android/as/oss/privateinference/networkusage",
        "//src/com/google/android/as/oss/privateinference/util/timers:annotations",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "bsa_modules",
    exports = [
        ":blind_sign_auth_module",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_modules",
    ],
)
