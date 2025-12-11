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
    name = "networkusage",
    srcs = ["PrivateInferenceNetworkUsageLogHelper.kt"],
    deps = [
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)

android_library(
    name = "impl",
    srcs = ["PrivateInferenceNetworkUsageLogHelperImpl.kt"],
    deps = [
        ":networkusage",
        "//src/com/google/android/as/oss/networkusage/api:connection_key_kt_proto_lite",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "module",
    srcs = ["PrivateInferenceNetworkUsageLogHelperModule.kt"],
    deps = [
        ":impl",
        ":networkusage",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "noop_impl",
    srcs = ["NoopNetworkUsageLogHelper.kt"],
    deps = [
        ":networkusage",
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "networkusage_jvm",
    srcs = [
        "NoopNetworkUsageLogHelper.kt",
        "PrivateInferenceNetworkUsageLogHelper.kt",
    ],
    deps = [
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)
