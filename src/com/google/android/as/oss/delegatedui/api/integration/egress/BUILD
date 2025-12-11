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

package(
    default_visibility = [
        "//visibility:public",
    ],
)

proto_library(
    name = "egress_proto",
    srcs = ["egress_integration.proto"],
    deps = [
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress/airflow:airflow_egress_integration_proto",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress/beacon:beacon_egress_proto",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress/bugle:bugle_egress_proto",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress/example:example_egress_proto",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress/sundog:sundog_egress_proto",
    ],
)

java_lite_proto_library(
    name = "egress_java_proto_lite",
    deps = [":egress_proto"],
)

kt_jvm_lite_proto_library(
    name = "egress_kt_proto_lite",
    deps = [":egress_proto"],
)
