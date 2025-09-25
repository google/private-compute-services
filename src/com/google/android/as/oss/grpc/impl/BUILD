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

package(default_visibility = ["//visibility:public"])

android_library(
    name = "client",
    srcs = [
        "OnDeviceChannelModule.java",
    ],
    deps = [
        ":policies",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/grpc:annotations",
        "//src/com/google/android/as/oss/grpc/config",
        "//src/com/google/android/as/oss/grpc/config/impl",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "server",
    srcs = [
        "GrpcServerEndpointConfiguratorImpl.java",
        "MetadataExtractionServerInterceptor.java",
        "ServerModule.java",
    ],
    deps = [
        ":policies",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/security/api:package_security_info_kt_proto_lite",
        "//src/com/google/android/as/oss/common/security/config",
        "//src/com/google/android/as/oss/grpc:api",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_lifecycle_lifecycle_service",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:io_grpc_grpc_context",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "policies",
    srcs = ["PcsSecurityPolicies.java"],
    deps = [
        "//src/com/google/android/as/oss/common/security/api:package_security_info_kt_proto_lite",
        "//src/com/google/android/as/oss/grpc:api",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
    ],
)
