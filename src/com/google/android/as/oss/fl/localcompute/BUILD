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

package(
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "resource_manager",
    srcs = ["LocalComputeResourceManager.java"],
    deps = ["@maven//:com_google_guava_guava"],
)

android_library(
    name = "file_copy_start_query",
    srcs = ["FileCopyStartQuery.java"],
    deps = [
        "//third_party/fcp/client:selector_context_java_proto_lite",
        "@federated_compute//fcp/client:fl_runner",
    ],
)

android_library(
    name = "utils",
    srcs = [
        "LocalComputeUtils.java",
        "PathConversionUtils.java",
    ],
    deps = [
        ":resource_manager",
        "//src/com/google/android/as/oss/fl/api:training_java_proto_lite",
        "@federated_compute//fcp/client:fl_runner",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "optional_module",
    srcs = ["OptionalBindsModule.java"],
    deps = [
        ":file_copy_start_query",
        ":resource_manager",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
