# Copyright 2024 Google LLC
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
        "HttpGrpcBindableService.java",
        "HttpGrpcModule.java",
    ],
    deps = [
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/flavor",
        "//src/com/google/android/as/oss/grpc:annotations",
        "//src/com/google/android/as/oss/grpc:api",
        "//src/com/google/android/as/oss/grpc:status",
        "//src/com/google/android/as/oss/http/api:http_grpc",
        "//src/com/google/android/as/oss/http/api:http_java_proto_lite",
        "//src/com/google/android/as/oss/http/config",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/config/impl",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//third_party/java/hilt:hilt-android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_api_grpc_proto_google_common_protos",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_lite",
        "@maven//:com_squareup_okhttp3_okhttp",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
    ],
)
