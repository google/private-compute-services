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
    "//src/com/google/android/as/oss/privateinference:__subpackages__",
])

android_library(
    name = "timers",
    srcs = ["Timers.kt"],
    deps = [
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "timers_jvm",
    srcs = ["Timers.kt"],
    deps = [
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)

android_library(
    name = "pi_debug_log_timers",
    srcs = ["PiDebugLogTimers.kt"],
    deps = [
        ":timers",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "trace_timers",
    srcs = ["TraceTimers.kt"],
    deps = [
        ":timers",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_client_timer_names",
        "//src/com/google/android/as/oss/privateinference/service:private_inference_grpc_timer_names",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "timer_set",
    srcs = ["TimerSet.kt"],
    deps = [
        ":timers",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "timer_set_jvm",
    srcs = ["TimerSet.kt"],
    deps = [
        ":timers_jvm",
        "//third_party/java/flogger",
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)

android_library(
    name = "annotations",
    srcs = ["Annotations.kt"],
    deps = [
        "@maven//:javax_inject_javax_inject",
    ],
)

# Make the BUILD file visible for open-source packaging.
exports_files(["BUILD"])
