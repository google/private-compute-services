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

package(default_visibility = ["//visibility:public"])

android_library(
    name = "feature_launcher_manifest",
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
)

android_library(
    name = "prod_modules",
    tags = [
        "keep_dep",
    ],
    exports = [
        ":feature_launcher_manifest",
        "//src/com/google/android/as/oss/featurelauncher/service:grpc_module",
    ],
)
