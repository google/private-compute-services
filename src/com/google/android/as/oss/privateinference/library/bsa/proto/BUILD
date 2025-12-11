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

load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl", "kt_jvm_lite_proto_library", "kt_jvm_proto_library")
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_grpc_library")

package(default_visibility = [
    "//javatests/com/google/android/apps/miphone/pcs/privateinference:__subpackages__",
    "//src/com/google/android/as/oss/privateinference:__subpackages__",
])

proto_library(
    name = "arateaipblinding_proto",
    srcs = ["arateaipblinding.proto"],
    has_services = 1,
    deps = [
        "//privacy/net/common/proto:attest_and_sign_proto",
        "//privacy/net/common/proto:get_initial_data_proto",
        "//privacy/net/common/proto:get_proxy_config_proto",
    ],
)

kt_jvm_lite_proto_library(
    name = "arateaipblinding_kt_proto_lite",
    deps = [":arateaipblinding_proto"],
)

kt_jvm_proto_library(
    name = "arateaipblinding_kt_proto",
    deps = [":arateaipblinding_proto"],
)

kt_jvm_grpc_library(
    name = "arateaipblinding_kt_grpc_lite",
    srcs = [":arateaipblinding_proto"],
    flavor = "lite",
    deps = [":arateaipblinding_kt_proto_lite"],
)

kt_jvm_grpc_library(
    name = "arateaipblinding_kt_grpc",
    srcs = [":arateaipblinding_proto"],
    deps = [":arateaipblinding_kt_proto"],
)

exports_files(["BUILD"])
