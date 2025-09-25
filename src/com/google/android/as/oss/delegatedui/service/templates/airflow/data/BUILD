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
load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_grpc_library")

package(default_visibility = [
])

android_library(
    name = "airflow_data_service_connection_module",
    srcs = ["AirflowDataServiceConnectionModule.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    deps = [
        ":airflow_data_service_kt_grpc",
        ":annotations",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/security:security_policy_utils",
        "//src/com/google/android/as/oss/common/security/api:package_security_info_kt_proto_lite",
        "//src/com/google/android/as/oss/common/security/config",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "annotations",
    srcs = ["Annotations.kt"],
    deps = ["@maven//:javax_inject_javax_inject"],
)

proto_library(
    name = "airflow_data_service_proto",
    srcs = ["airflow_data_service.proto"],
    has_services = True,
    deps = [""],
)

java_lite_proto_library(
    name = "airflow_data_service_java_proto_lite",
    deps = [":airflow_data_service_proto"],
)

kt_jvm_lite_proto_library(
    name = "airflow_data_service_kt_proto_lite",
    deps = [":airflow_data_service_proto"],
)

java_grpc_library(
    name = "airflow_data_service_java_grpc",
    srcs = [":airflow_data_service_proto"],
    constraints = ["android"],
    flavor = "lite",
    deps = [":airflow_data_service_java_proto_lite"],
)

kt_jvm_grpc_library(
    name = "airflow_data_service_kt_grpc",
    srcs = [":airflow_data_service_proto"],
    flavor = "lite",
    deps = [":airflow_data_service_kt_proto_lite"],
)
