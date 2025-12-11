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
load("//third_party/bazel_rules/rules_python/python:proto.bzl", "py_proto_library")
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl", "kt_jvm_lite_proto_library")
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_grpc_library")

package(
    default_visibility = [
        "//visibility:public",
    ],
)

proto_library(
    name = "data_service_proto",
    srcs = [
        "data_service.proto",
        "data_service_get_additional_data.proto",
        "data_service_get_template_data.proto",
        "data_service_log_usage_data.proto",
    ],
    has_services = True,
    deps = [
        "",
        "//src/com/google/android/as/oss/delegatedui/api/common:common_proto",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:templates_proto",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:ui_id_token_proto",
        "//third_party/protobuf:cpp_features_proto",
    ],
)

java_lite_proto_library(
    name = "data_service_java_proto_lite",
    deps = [":data_service_proto"],
)

kt_jvm_lite_proto_library(
    name = "data_service_kt_proto_lite",
    deps = [":data_service_proto"],
)

java_grpc_library(
    name = "data_service_java_grpc",
    srcs = [":data_service_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [":data_service_java_proto_lite"],
)

kt_jvm_grpc_library(
    name = "data_service_kt_grpc",
    srcs = [":data_service_proto"],
    flavor = "lite",
    deps = [":data_service_kt_proto_lite"],
)

py_proto_library(
    name = "data_service_py_pb2",
    deps = [":data_service_proto"],
)

android_library(
    name = "parcelable_keys",
    srcs = ["DelegatedUiDataServiceParcelableKeys.kt"],
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/delegatedui/utils:parcelable_key",
        "//src/com/google/android/as/oss/delegatedui/utils:server_interceptor",
    ],
)
