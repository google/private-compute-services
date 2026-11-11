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
load("@rules_proto_grpc//java:defs.bzl", "java_grpc_library")
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl", "kt_jvm_lite_proto_library")

package(default_visibility = [
    "//javatests/com/google/android/apps/miphone/pcs/asr:__subpackages__",
    "//src/com/google/android/as/oss:__subpackages__",
    "//src/com/google/android/as/oss/asr:__subpackages__",
])

proto_library(
    name = "srsg_proxy_proto",
    srcs = ["srsg_proxy.proto"],
    has_services = True,
    deps = [
        "",
        "srsg_feature_config_proto",
    ],
)

java_lite_proto_library(
    name = "srsg_proxy_java_proto_lite",
    deps = [":srsg_proxy_proto"],
)

kt_jvm_lite_proto_library(
    name = "srsg_proxy_kt_proto_lite",
    deps = [":srsg_proxy_proto"],
)

java_grpc_library(
    name = "srsg_proxy_java_grpc_lite",
    srcs = [":srsg_proxy_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [":srsg_proxy_java_proto_lite"],
)

proto_library(
    name = "srsg_service_proto",
    srcs = ["srsg_service.proto"],
    has_services = True,
    deps = [
        "",
        "srsg_feature_config_proto",
    ],
)

java_lite_proto_library(
    name = "srsg_service_java_proto_lite",
    deps = [":srsg_service_proto"],
)

java_grpc_library(
    name = "srsg_service_java_grpc_lite",
    srcs = [":srsg_service_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [":srsg_service_java_proto_lite"],
)

proto_library(
    name = "srsg_feature_config_proto",
    srcs = ["srsg_feature_config.proto"],
    has_services = True,
    deps = [
        "",
    ],
)

java_lite_proto_library(
    name = "srsg_feature_config_java_proto_lite",
    deps = [":srsg_feature_config_proto"],
)

java_grpc_library(
    name = "srsg_feature_config_java_grpc_lite",
    srcs = [":srsg_feature_config_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [":srsg_feature_config_java_proto_lite"],
)

# Optional: Wrapper library for convenience
android_library(
    name = "api",
    exports = [
        ":srsg_feature_config_java_grpc_lite",
        ":srsg_feature_config_java_proto_lite",
        ":srsg_proxy_java_grpc_lite",
        ":srsg_proxy_java_proto_lite",
        ":srsg_service_java_grpc_lite",
        ":srsg_service_java_proto_lite",
    ],
)
