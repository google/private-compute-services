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
    name = "flags_impl",
    srcs = [
        "DeviceFlagManagerFactory.java",
        "FlagManagerModule.java",
    ],
    deps = [
        ":flag_manager_impl",
        "//src/com/google/android/as/oss/common/config",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "flag_manager_impl",
    srcs = [
        "DeviceConfigProxy.java",
        "DeviceFlagManager.java",
    ],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:org_checkerframework_checker_qual",
    ],
)

android_library(
    name = "pcs_common_config",
    srcs = [
        "PcsCommonConfig.java",
        "PcsCommonConfigReader.java",
    ],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "//third_party/java/auto:auto_value",
    ],
)

android_library(
    name = "pcs_common_config_module",
    srcs = [
        "PcsCommonConfigModule.java",
    ],
    deps = [
        ":pcs_common_config",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
