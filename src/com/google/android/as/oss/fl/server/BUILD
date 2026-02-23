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
    name = "service",
    srcs = [
        "PrivateLoggerGrpcBindableService.java",
        "PrivateLoggerGrpcModule.java",
        "TrainerGrpcBindableService.java",
        "TrainerGrpcModule.java",
    ],
    deps = [
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/fl/api:private_logging_grpc",
        "//src/com/google/android/as/oss/fl/api:private_logging_java_proto_lite",
        "//src/com/google/android/as/oss/fl/api:training_grpc",
        "//src/com/google/android/as/oss/fl/api:training_java_proto_lite",
        "//src/com/google/android/as/oss/fl/fc/service/scheduler",
        "//src/com/google/android/as/oss/fl/fc/service/scheduler/endorsementoptions",
        "//src/com/google/android/as/oss/fl/fc/service/scheduler/endorsementoptions:endorsement_client_type",
        "//src/com/google/android/as/oss/fl/federatedcompute/config",
        "//src/com/google/android/as/oss/fl/localcompute:file_copy_start_query",
        "//src/com/google/android/as/oss/fl/localcompute:optional_module",
        "//src/com/google/android/as/oss/fl/localcompute:resource_manager",
        "//src/com/google/android/as/oss/fl/localcompute:utils",
        "//src/com/google/android/as/oss/grpc:annotations",
        "//third_party/fcp/client:selector_context_java_proto_lite",
        "//third_party/fcp/java_src/main/java/com/google/fcp/client/privatelogger",
        "//third_party/fcp/java_src/main/java/com/google/fcp/client/privatelogger/impl",
        "//third_party/fcp/protos/confidentialcompute:access_policy_endorsement_options_java_proto_lite",
        "//third_party/java/proto:any_java_proto_lite",
        "@federated_compute//fcp/client:fl_runner",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_checkerframework_checker_qual",
    ],
)

android_library(
    name = "service_for_tests",
    testonly = True,
    visibility = ["//visibility:public"],
    exports = [
        ":service",
    ],
)
