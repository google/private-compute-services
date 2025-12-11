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
    "//javatests/com/google/android/apps/miphone/pcs/privateinference:__subpackages__",
    "//src/com/google/android/as/oss/privateinference:__subpackages__",
])

android_library(
    name = "android_keystore_attester_impl",
    srcs = [
        "AndroidKeystoreAttesterImpl.kt",
    ],
    manifest = ":AndroidManifest.xml",
    deps = [
        "//third_party/kotlin/kotlinx_atomicfu:kotlinx_atomicfu-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

# Make the BUILD file visible for open-source packaging.
exports_files(["BUILD"])
