# Copyright 2023 Google LLC
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

# Localization

android_library(
    name = "user",
    srcs = glob(["*.java"]),
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/com/google/android/as/oss/networkusage/api:connection_key_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//third_party/java/android_libs/settingslib:footerpreference",
        "//third_party/java/android_libs/settingslib:mainswitchpreference",
        "//third_party/java/android_libs/settingslib:settingstheme",
        "//third_party/java/android_libs/settingslib/collapsingtoolbarbaseactivity",
        "//third_party/java/auto:auto_value",
        "//third_party/java/hilt:hilt-android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_fragment_fragment",
        "@maven//:androidx_swiperefreshlayout_swiperefreshlayout",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_checkerframework_checker_qual",
    ],
)

android_library(
    name = "res_lib",
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party/java/android_libs/settingslib:footerpreference",
        "//third_party/java/android_libs/settingslib:mainswitchpreference",
        "//third_party/java/android_libs/settingslib:settingstheme",
        "//third_party/java/android_libs/settingslib:topintropreference",
    ],
)
