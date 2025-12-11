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
    name = "annotations",
    srcs = ["Annotations.java"],
    deps = ["@maven//:javax_inject_javax_inject"],
)

android_library(
    name = "api",
    srcs = [
        "ContextKeys.java",
        "GrpcServerEndpointConfiguration.java",
        "GrpcServerEndpointConfigurator.java",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_lifecycle_lifecycle_service",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:io_grpc_grpc_context",
    ],
)

android_library(
    name = "grpc",
    srcs = [
        "PcsGrpcModule.java",
        "PcsGrpcService.java",
    ],
    deps = [
        ":annotations",
        ":api",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/grpc/config",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_lifecycle_lifecycle_service",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "grpc_pcs",
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    visibility = ["//visibility:public"],
    deps = [
        ":grpc",
    ],
)

android_library(
    name = "status",
    srcs = [
        "GrpcStatusProto.java",
    ],
    deps = [
        "//third_party/java/proto:any_java_proto_lite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_api_grpc_proto_google_common_protos",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_lite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_protobuf_lite",
    ],
)
