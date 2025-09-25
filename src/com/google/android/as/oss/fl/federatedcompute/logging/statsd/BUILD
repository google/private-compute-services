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
    name = "impl",
    srcs = [
        "FcClientStatsdLogManager.java",
        "TfErrorParser.java",
    ],
    deps = [
        "//logs/proto/wireless/android/play/playlog/fc:fc_log_proto_lite",
        "//logs/proto/wireless/android/play/playlog/fc:secagg_log_java_proto_lite",
        "//src/com/google/android/as/oss/fl/federatedcompute/config",
        "//src/com/google/android/as/oss/fl/federatedcompute/logging",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/logging/converter",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//third_party/fcp/client:histogram_counters_java_proto_lite",
        "//third_party/java/androidx/legacy/support_core_utils",
        "@federated_compute//fcp/client:fl_runner",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_lite",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "module",
    srcs = ["FcpStatsdLoggingModule.java"],
    deps = [
        ":impl",
        "//src/com/google/android/as/oss/fl/federatedcompute/logging",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
