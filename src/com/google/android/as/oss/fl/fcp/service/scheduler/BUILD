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
    name = "scheduler",
    srcs = glob(["*.java"]),
    manifest = "//src/com/google/android/as/oss/fl/fc/service:AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/fl/api:training_java_proto_lite",
        "//src/com/google/android/as/oss/fl/fc/service/scheduler/endorsementoptions",
        "//src/com/google/android/as/oss/fl/fc/service/scheduler/endorsementoptions:endorsement_client_type",
        "//src/com/google/android/as/oss/fl/federatedcompute/config",
        "//src/com/google/android/as/oss/fl/federatedcompute/util",
        "//src/com/google/android/as/oss/fl/localcompute:utils",
        "//third_party/fcp/protos/confidentialcompute:access_policy_endorsement_options_java_proto_lite",
        "@federated_compute//fcp/client:fl_runner",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
    ],
)
