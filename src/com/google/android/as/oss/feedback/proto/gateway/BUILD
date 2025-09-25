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

load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl", "kt_jvm_lite_proto_library")

package(default_visibility = ["//visibility:public"])

proto_library(
    name = "spoon_proto",
    srcs = ["spoon.proto"],
    deps = [""],
)

java_lite_proto_library(
    name = "spoon_java_proto_lite",
    deps = [":spoon_proto"],
)

kt_jvm_lite_proto_library(
    name = "spoon_kt_proto_lite",
    deps = [":spoon_proto"],
)

proto_library(
    name = "quartz_log_proto",
    srcs = ["quartz_log.proto"],
    deps = [
        "",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

kt_jvm_lite_proto_library(
    name = "quartz_log_kt_proto_lite",
    deps = [":quartz_log_proto"],
)

java_lite_proto_library(
    name = "quartz_log_java_proto_lite",
    deps = [":quartz_log_proto"],
)

proto_library(
    name = "message_armour_proto",
    srcs = ["message_armour.proto"],
    deps = [""],
)

java_lite_proto_library(
    name = "message_armour_java_proto_lite",
    deps = [":message_armour_proto"],
)

kt_jvm_lite_proto_library(
    name = "message_armour_kt_proto_lite",
    deps = [":message_armour_proto"],
)

proto_library(
    name = "pixel_apex_api_proto",
    srcs = ["pixel_apex_api.proto"],
    deps = [
        "",
        ":message_armour_proto",
        ":quartz_log_proto",
        ":spoon_proto",
    ],
)

java_lite_proto_library(
    name = "pixel_apex_api_java_proto_lite",
    deps = [":pixel_apex_api_proto"],
)

kt_jvm_lite_proto_library(
    name = "pixel_apex_api_kt_proto_lite",
    deps = [":pixel_apex_api_proto"],
)
