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

load("@rules_proto_grpc//java:defs.bzl", "java_grpc_library")
load("//third_party/protobuf/bazel:cc_proto_library.bzl", "cc_proto_library")
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:java_proto_library.bzl", "java_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl", "kt_jvm_lite_proto_library")

package(default_visibility = [
    "//visibility:public",
])

proto_library(
    name = "anonymous_token_request_proto",
    srcs = ["anonymous_token_request.proto"],
    deps = [
        "",
        "//third_party/protobuf:cpp_features_proto",
    ],
)

cc_proto_library(
    name = "anonymous_token_request_cc_proto",
    deps = [":anonymous_token_request_proto"],
)

java_proto_library(
    name = "anonymous_token_request_java_proto",
    deps = [":anonymous_token_request_proto"],
)

kt_jvm_lite_proto_library(
    name = "anonymous_token_request_kt_proto_lite",
    deps = [":anonymous_token_request_proto"],
)

java_lite_proto_library(
    name = "anonymous_token_request_java_proto_lite",
    deps = [":anonymous_token_request_proto"],
)

proto_library(
    name = "private_aratea_service_proto",
    srcs = [
        "private_aratea_service.proto",
    ],
    has_services = True,
    deps = [
        "",
        ":anonymous_token_request_proto",
        ":device_attestation_request_proto",
        "//third_party/oak/proto/session:session_proto",
    ],
)

proto_library(
    name = "device_attestation_request_proto",
    srcs = [
        "device_attestation_request.proto",
    ],
    deps = [
        "",
        "//third_party/protobuf:cpp_features_proto",
    ],
)

java_lite_proto_library(
    name = "device_attestation_request_java_proto_lite",
    deps = [":device_attestation_request_proto"],
)

kt_jvm_lite_proto_library(
    name = "device_attestation_request_kt_proto_lite",
    deps = [":device_attestation_request_proto"],
)

cc_proto_library(
    name = "device_attestation_request_cc_proto",
    deps = [":device_attestation_request_proto"],
)

java_lite_proto_library(
    name = "private_aratea_service_java_proto_lite",
    deps = [":private_aratea_service_proto"],
)

kt_jvm_lite_proto_library(
    name = "private_aratea_service_kt_proto_lite",
    deps = [":private_aratea_service_proto"],
)

java_grpc_library(
    name = "private_aratea_service_java_lite_grpc",
    srcs = [":private_aratea_service_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [":private_aratea_service_java_proto_lite"],
)
