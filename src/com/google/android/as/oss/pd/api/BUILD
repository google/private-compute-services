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
load("//third_party/bazel_rules/rules_python/python:proto.bzl", "py_proto_library")
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load(
    "//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl",
    "kt_jvm_lite_proto_library",
    "kt_jvm_proto_library",
)

package(default_visibility = [
    "//visibility:public",
])

proto_library(
    name = "protected_download_proto",
    srcs = [
        "protected_download.proto",
    ],
    has_services = 1,
    option_deps = ["//third_party/protobuf:cpp_features_proto"],
    visibility = [
        "//visibility:public",
    ],
    deps = [""],
)

java_lite_proto_library(
    name = "protected_download_java_proto_lite",
    deps = [
        ":protected_download_proto",
    ],
)

kt_jvm_lite_proto_library(
    name = "protected_download_kt_proto_lite",
    deps = [
        ":protected_download_proto",
    ],
)

kt_jvm_proto_library(
    name = "protected_download_kt_proto",
    deps = [":protected_download_proto"],
)

py_proto_library(
    name = "protected_download_py_pb2",
    deps = [":protected_download_proto"],
)

java_grpc_library(
    name = "protected_download_grpc",
    srcs = [":protected_download_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [
        ":protected_download_java_proto_lite",
    ],
)
