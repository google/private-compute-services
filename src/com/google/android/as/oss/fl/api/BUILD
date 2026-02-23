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
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load(
    "//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl",
    "kt_jvm_lite_proto_library",
)

package(default_visibility = ["//visibility:public"])

proto_library(
    name = "training_proto",
    srcs = ["training.proto"],
    has_services = True,
)

java_lite_proto_library(
    name = "training_java_proto_lite",
    deps = [":training_proto"],
)

kt_jvm_lite_proto_library(
    name = "training_kt_proto_lite",
    deps = [":training_proto"],
)

java_grpc_library(
    name = "training_grpc",
    srcs = [":training_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [
        ":training_java_proto_lite",
    ],
)

proto_library(
    name = "private_logging_proto",
    srcs = ["private_logging.proto"],
    has_services = True,
    deps = [
        "",
        "//third_party/fcp/client:selector_context_proto",
    ],
)

java_lite_proto_library(
    name = "private_logging_java_proto_lite",
    deps = [":private_logging_proto"],
)

kt_jvm_lite_proto_library(
    name = "private_logging_kt_proto_lite",
    deps = [":private_logging_proto"],
)

java_grpc_library(
    name = "private_logging_grpc",
    srcs = [":private_logging_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [
        ":private_logging_java_proto_lite",
    ],
)
