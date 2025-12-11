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
    name = "heartbeat_service",
    srcs = ["HeartbeatService.java"],
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/config/impl:pcs_common_config",
        "//src/com/google/android/as/oss/common/flavor",
        "//src/com/google/android/as/oss/fl/api:training_java_proto_lite",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/config",
        "//src/com/google/android/as/oss/fl/federatedcompute/training",
        "//src/com/google/android/as/oss/fl/populations",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_core_core",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "heartbeat_module",
    srcs = ["HeartbeatServiceModule.java"],
    deps = [
        ":heartbeat_service",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/config/impl:pcs_common_config",
        "//src/com/google/android/as/oss/common/initializer",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "worker_factories_module",
    srcs = ["WorkerFactoriesModule.kt"],
    deps = [
        "//third_party/java/androidx/work",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
