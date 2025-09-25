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

package(default_visibility = [
    "//visibility:public",
])

proto_library(
    name = "client_persistent_state_proto",
    srcs = ["client_persistent_state.proto"],
)

java_lite_proto_library(
    name = "client_persistent_state_java_proto_lite",
    deps = [":client_persistent_state_proto"],
)

android_library(
    name = "persistence",
    srcs = glob(["*.java"]),
    deps = [
        ":client_persistent_state_java_proto_lite",
        "@maven//:com_google_guava_guava",
    ],
)
