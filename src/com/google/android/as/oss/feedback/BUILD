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

package_group(
    name = "feedback_api_visibility",
    includes = ["//visibility:public"],
)

filegroup(
    name = "manifest",
    srcs = ["AndroidManifest.xml"],
)

android_library(
    name = "feedback",
    srcs = [
        "FeedbackApi.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest_api.xml",
    visibility = [":feedback_api_visibility"],
    deps = [
        "//src/com/google/android/as/oss/feedback/proto:entity_feedback_dialog_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:multi_feedback_dialog_data_kt_proto_lite",
        "//third_party/java/androidx/appcompat",
    ],
)

android_library(
    name = "prod_modules",
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    tags = [
        "keep_dep",
    ],
    exports = [
        ":feedback_activity",
        "//src/com/google/android/as/oss/feedback/serviceclient/serviceconnection:module",
    ],
)

android_library(
    name = "feedback_activity",
    srcs = [
        "FeedbackActivity.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":feedback",
        ":feedback_ui_state",
        ":feedback_view_model",
        ":multi_entity_feedback_dialog",
        ":single_entity_feedback_dialog",
        "//src/com/google/android/as/oss/feedback/proto:entity_feedback_dialog_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:multi_feedback_dialog_data_kt_proto_lite",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/runtime",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
    ],
)

android_library(
    name = "single_entity_feedback_dialog",
    srcs = [
        "SingleEntityFeedbackDialog.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":feedback_opt_in_content",
        ":feedback_submission_data",
        ":feedback_ui_state",
        ":feedback_view_model",
        ":view_feedback_data_dialog",
        "//src/com/google/android/as/oss/delegatedui/service/templates/fonts:flex_fonts",
        "//src/com/google/android/as/oss/feedback/proto:entity_feedback_dialog_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/serviceclient:service_client",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/runtime/saveable",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/hilt/navigation_compose",
    ],
)

android_library(
    name = "multi_entity_feedback_dialog",
    srcs = ["MultiEntityFeedbackDialog.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":feedback_opt_in_content",
        ":feedback_submission_data",
        ":feedback_ui_state",
        ":feedback_view_model",
        ":view_feedback_data_dialog",
        "//src/com/google/android/as/oss/delegatedui/service/templates/fonts:flex_fonts",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:multi_feedback_dialog_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:quartz_log_kt_proto_lite",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/hilt/navigation_compose",
        "//third_party/java/androidx/lifecycle/viewmodel_compose",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
    ],
)

android_library(
    name = "feedback_view_model",
    srcs = ["FeedbackViewModel.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":feedback_submission_data",
        ":feedback_ui_state",
        "//src/com/google/android/as/oss/common",
        "//src/com/google/android/as/oss/feedback/gateway:feedback_http_client",
        "//src/com/google/android/as/oss/feedback/gateway:feedback_http_client_module",  # buildcleaner: keep for Feedback Http Client Module
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:pixel_apex_api_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:quartz_log_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/quartz/serviceclient:quartz_service_client",
        "//src/com/google/android/as/oss/feedback/quartz/utils:quartz_data_helper",
        "//src/com/google/android/as/oss/feedback/serviceclient:service_client",
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
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "view_feedback_data_dialog",
    srcs = ["ViewFeedbackDataDialog.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":feedback_ui_state",
        ":feedback_view_model",
        ":view_feedback_data",
        "//src/com/google/android/as/oss/feedback/quartz/serviceclient:quartz_service_client",
        "//src/com/google/android/as/oss/feedback/serviceclient:service_client",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
    ],
)

android_library(
    name = "feedback_opt_in_content",
    srcs = ["FeedbackOptInContent.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":feedback_ui_state",
        ":feedback_view_model",
        "//src/com/google/android/as/oss/feedback/quartz/serviceclient:quartz_service_client",
        "//src/com/google/android/as/oss/feedback/serviceclient:service_client",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
    ],
)

android_library(
    name = "feedback_ui_state",
    srcs = ["FeedbackUiState.kt"],
    deps = [
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
