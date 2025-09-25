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

# BUILD file for the Program Blobs Protos.
load("@rules_proto_grpc//java:defs.bzl", "java_grpc_library")
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:java_proto_library.bzl", "java_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load(
    "//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl",
    "kt_jvm_lite_proto_library",
    "kt_jvm_proto_library",
)
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_grpc_library")

package(
    default_visibility = ["//visibility:public"],
)

proto_library(
    name = "programblobs_proto",
    srcs = ["programblobs.proto"],
    has_services = 1,
    deps = [
        "//google/api:annotations",
    ],
)

java_proto_library(
    name = "programblobs_java_proto",
    deps = [":programblobs_proto"],
)

java_lite_proto_library(
    name = "programblobs_java_proto_lite",
    deps = [":programblobs_proto"],
)

kt_jvm_lite_proto_library(
    name = "programblobs_kt_proto_lite",
    deps = [":programblobs_proto"],
)

java_grpc_library(
    name = "programblobs_java_grpc_lite",
    srcs = [":programblobs_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [
        ":programblobs_java_proto_lite",
    ],
)

kt_jvm_grpc_library(
    name = "programblobs_kt_grpc_lite",
    srcs = [":programblobs_proto"],
    flavor = "lite",
    deps = [":programblobs_kt_proto_lite"],
)

kt_jvm_proto_library(
    name = "programblobs_kt_proto",
    deps = [":programblobs_proto"],
)

kt_jvm_grpc_library(
    name = "programblobs_kt_grpc",
    srcs = [":programblobs_proto"],
    deps = [":programblobs_kt_proto"],
)

proto_library(
    name = "download_service_proto",
    srcs = ["download_service.proto"],
    has_services = 1,
    deps = [
        "",
        "//google/api:annotations",
    ],
)

java_proto_library(
    name = "download_service_java_proto",
    deps = [":download_service_proto"],
)

java_lite_proto_library(
    name = "download_service_java_proto_lite",
    deps = [":download_service_proto"],
)

kt_jvm_lite_proto_library(
    name = "download_service_kt_proto_lite",
    deps = [":download_service_proto"],
)

java_grpc_library(
    name = "download_service_java_grpc_lite",
    srcs = [":download_service_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [
        ":download_service_java_proto_lite",
    ],
)

kt_jvm_grpc_library(
    name = "download_service_kt_grpc_lite",
    srcs = [":download_service_proto"],
    flavor = "lite",
    deps = [":download_service_kt_proto_lite"],
)

kt_jvm_proto_library(
    name = "download_service_kt_proto",
    deps = [":download_service_proto"],
)

kt_jvm_grpc_library(
    name = "download_service_kt_grpc",
    srcs = [":download_service_proto"],
    deps = [":download_service_kt_proto"],
)
