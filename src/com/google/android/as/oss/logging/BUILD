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
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")

package(default_visibility = ["//visibility:public"])

proto_library(
    name = "enums_proto",
    srcs = [
        "enums.proto",
    ],
)

proto_library(
    name = "atoms_proto",
    srcs = [
        "atoms.proto",
    ],
    deps = [
        ":enums_proto",
    ],
)

java_lite_proto_library(
    name = "enums_java_proto_lite",
    deps = [":enums_proto"],
)

java_lite_proto_library(
    name = "atoms_java_proto_lite",
    deps = [":atoms_proto"],
)

android_library(
    name = "api",
    srcs = [
        "PcsStatsLog.java",
    ],
    deps = [
        ":atoms_java_proto_lite",
    ],
)
