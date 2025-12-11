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
    "//java/com/google/android/apps/internal/mdi/privatearatea:__pkg__",
    "//java/com/google/android/libraries/mdi/privatearatea:__pkg__",
    "//javatests/com/google/android/libraries/mdi/privatearatea:__pkg__",
    "//visibility:public",
])

android_library(
    name = "logging",
    srcs = [
        "PcsStatsLogger.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
    ],
)

android_library(
    name = "logging_impl",
    srcs = ["PcsStatsLoggerImpl.kt"],
    deps = [
        ":duration_bucket_logic",
        ":logging",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "logging_module",
    srcs = ["ProdLoggerModule.kt"],
    deps = [
        ":logging",
        ":logging_impl",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "noop_module",
    srcs = [
        "NoopLoggerModule.kt",
        "NoopPcsStatsLogger.kt",
    ],
    deps = [
        ":logging",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "duration_bucket_logic",
    srcs = [
        "DurationBucketLogic.kt",
    ],
)
