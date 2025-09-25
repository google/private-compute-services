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
load("//java/com/google/android/as/oss/asi:build_i18n.bzl", "LOCALES_SETTINGS", "TC_PROJECT_SMART_SUGGESTIONS")
load("//tools/build_defs:tc.bzl", "TCExtractMessages", "TCGenerateLocalizedFiles")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "resources",
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]) + [":localized_resources"],
    deps = [
        "//googledata/icons/android/m1_extended/navigation/close:grey600_24",
        "//java/com/google/android/assets/googleg:light_color_24_lib",
        "//java/com/google/android/assets/googleg:standard_color_24_lib",
        "//java/com/google/android/libraries/material/gm3/theme",
        "@maven//:com_google_android_material_material",
    ],
)

LOCALIZATION_SRCS = [
    "res/values/strings.xml",
]

TCExtractMessages(
    name = "xmb_files",
    handler = "android",
    project = TC_PROJECT_SMART_SUGGESTIONS,
    source_dir = "java/com/google/android/apps/miphone/pcs/survey/service/gm3ui",
    source_files = LOCALIZATION_SRCS,
)

TCGenerateLocalizedFiles(
    name = "localized_resources",
    srcs = LOCALIZATION_SRCS,
    handler = "android",
    locales = LOCALES_SETTINGS,
    options = {"localizedFileNameStyle": "android"},
    project = TC_PROJECT_SMART_SUGGESTIONS,
)
