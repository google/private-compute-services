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
    name = "feedback_ui_consts",
    srcs = ["FeedbackUiElementType.kt"],
    deps = [],
)

android_library(
    name = "feedback_view_model",
    srcs = ["FeedbackViewModel.kt"],
    deps = [
        ":feedback_submission_data",
        ":feedback_ui_state",
        ":ground_truth_data",
        ":view_feedback_data",
        "//src/com/google/android/as/oss/common",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:ui_id_token_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/config",
        "//src/com/google/android/as/oss/feedback/gateway:feedback_http_client",
        "//src/com/google/android/as/oss/feedback/gateway:feedback_http_client_module",  # buildcleaner: keep for Feedback Http Client Module
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:pixel_apex_api_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:quartz_log_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/quartz/serviceclient:quartz_service_client",
        "//src/com/google/android/as/oss/feedback/quartz/utils:quartz_data_helper",
        "//src/com/google/android/as/oss/feedback/serviceclient:service_client",
        "//src/com/google/android/as/oss/logging/uiusage:internal_only_modules",
        "//src/com/google/android/as/oss/logging/uiusage/api:usage_data_service_kt_grpc",
        "//src/com/google/android/as/oss/logging/uiusage/api:usage_data_service_kt_proto_lite",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/hilt/navigation_compose",
        "//third_party/java/dagger/hilt:view_model",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "feedback_ui_state",
    srcs = ["FeedbackUiState.kt"],
    deps = [
        ":ground_truth_data",
        ":view_feedback_data",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/quartz/serviceclient:quartz_service_client",
        "//src/com/google/android/as/oss/feedback/serviceclient:service_client",
    ],
)

android_library(
    name = "feedback_submission_data",
    srcs = ["FeedbackSubmissionData.kt"],
    deps = [
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:quartz_log_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
    ],
)

android_library(
    name = "view_feedback_data",
    srcs = ["ViewFeedbackData.kt"],
    deps = [],
)

android_library(
    name = "ground_truth_data",
    srcs = ["GroundTruthData.kt"],
    deps = [],
)
