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
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_grpc_library")

package(default_visibility = [
    "//javatests/com/google/android/apps/miphone/pcs/featurelauncher:__subpackages__",
    "//src/com/google/android/as/oss/featurelauncher:__subpackages__",
    "//visibility:public",
])

proto_library(
    name = "feature_launcher_proto",
    srcs = ["feature_launcher.proto"],
    has_services = True,
    deps = [""],
)

java_lite_proto_library(
    name = "feature_launcher_java_proto_lite",
    deps = [":feature_launcher_proto"],
)

kt_jvm_lite_proto_library(
    name = "feature_launcher_kt_proto_lite",
    deps = [":feature_launcher_proto"],
)

kt_jvm_grpc_library(
    name = "feature_launcher_kt_grpc",
    srcs = [":feature_launcher_proto"],
    flavor = "lite",
    deps = [":feature_launcher_kt_proto_lite"],
)
