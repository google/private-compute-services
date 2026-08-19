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
        "CallingPackageNameProvider.java",
        "PrivateLoggerGrpcBindableService.java",
        "PrivateLoggerGrpcModule.java",
        "TrainerGrpcBindableService.java",
        "TrainerGrpcModule.java",
    ],
    deps = [
        "//java/com/google/frameworks/client/data/android/server:suppress_peer_uid",
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
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//src/com/google/android/as/oss/protos:pcs_feature_enum_java_proto_lite",
        "//third_party/fcp/java_src/main/java/com/google/fcp/client/privatelogger",
        "//third_party/fcp/java_src/main/java/com/google/fcp/client/privatelogger/impl",
        "@federated_compute//fcp/client:fl_runner",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_lite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
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
