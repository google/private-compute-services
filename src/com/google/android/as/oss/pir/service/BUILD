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
    srcs = glob(
        ["*.java"],
    ),
    deps = [
        "//java/com/google/common/android/base:ticker",
        "//logs/proto/wireless/android/play/playlog/privateretrieval:private_retrieval_log_java_proto_lite",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/flavor",
        "//src/com/google/android/as/oss/grpc:annotations",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//src/com/google/android/as/oss/pir/api:pir_grpc",
        "//src/com/google/android/as/oss/pir/api:pir_java_proto_lite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_lite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_context",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_checkerframework_checker_qual",
        "@private_retrieval//private_retrieval/java:pir",
        "@private_retrieval//private_retrieval/java/core",
    ],
)
