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
    name = "common",
    srcs =
        [
            "CoroutineModule.kt",
            "Executors.java",
            "ExecutorsModule.java",
        ],
    deps = [
        ":annotation",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "annotation",
    srcs =
        [
            "CoroutineQualifiers.kt",
            "ExecutorAnnotations.java",
        ],
    deps = [
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject",
    ],
)

exports_files(
    [
        "AndroidManifest.xml",
    ],
    visibility = ["//visibility:public"],
)
