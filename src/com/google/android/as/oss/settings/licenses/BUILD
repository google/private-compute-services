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

android_library(
    name = "licenses",
    srcs = glob(["*.java"]),
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    # This whole target is branched from
    # //java/com/google/android/libraries/social/licenses
    # which does not have null safety at this time. In order to not diverge from
    # the source too much, we suppress nullness checks here.
    suppression = True,
    visibility = ["//visibility:public"],
    deps = [
        "//third_party/java/androidx/appcompat",
        "@maven//:com_google_guava_guava",
    ],
)

# Make the BUILD file visible for open-source packaging.
exports_files(["BUILD"])
