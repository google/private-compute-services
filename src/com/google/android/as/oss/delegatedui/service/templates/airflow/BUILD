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
    name = "airflow",
    srcs = [
        "AirFlowTemplateRenderer.kt",
    ],
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress/airflow:airflow_egress_integration_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:templates_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/templates",
        "//src/com/google/android/as/oss/delegatedui/service/templates/airflow/data:airflow_data_service_kt_grpc",
        "//src/com/google/android/as/oss/delegatedui/service/templates/airflow/data:airflow_data_service_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/templates/airflow/data:annotations",
        "//src/com/google/android/as/oss/delegatedui/service/templates/scope",
        "//src/com/google/android/as/oss/delegatedui/utils:response_with_parcelables",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "airflow_template_renderer_module",
    srcs = ["AirFlowTemplaterHandlerModule.kt"],
    deps = [
        ":airflow",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:templates_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/templates",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "module",
    exports = [
        ":airflow_template_renderer_module",
        "//src/com/google/android/as/oss/delegatedui/service/templates/airflow/data:airflow_data_service_connection_module",
    ],
)
