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
    name = "service",
    srcs = [
        "SurveyConfirmActivity.java",
        "SurveyGrpcBindableService.java",
        "SurveyGrpcModule.java",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/flavor",
        "//src/com/google/android/as/oss/grpc:annotations",
        "//src/com/google/android/as/oss/grpc:status",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/config/impl",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//src/com/google/android/as/oss/survey/api:survey_grpc",
        "//src/com/google/android/as/oss/survey/api:survey_java_proto_lite",
        "//src/com/google/android/as/oss/survey/config",
        "//src/com/google/android/as/oss/survey/service/ui:resources",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_core_core",
        "@maven//:androidx_lifecycle_lifecycle_service",
        "@maven//:com_google_android_material_material",
        "@maven//:com_google_api_grpc_proto_google_common_protos",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:com_google_protobuf_protobuf_lite",
        "@maven//:com_squareup_okhttp3_okhttp",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
    ],
)
