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
    name = "single_entity_feedback_dialog",
    srcs = [
        "SingleEntityFeedbackDialog.kt",
        "SingleEntityFeedbackDialogV1.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":feedback_content_scaffold",
        ":feedback_opt_in_content",
        ":feedback_tag_chips",
        ":same_size_layout",
        ":view_feedback_data_dialog",
        "//src/com/google/android/as/oss/delegatedui/service/templates/fonts:flex_fonts",
        "//src/com/google/android/as/oss/delegatedui/service/templates/motion:expressive_motion",
        "//src/com/google/android/as/oss/feedback/domain:feedback_submission_data",
        "//src/com/google/android/as/oss/feedback/domain:feedback_ui_consts",
        "//src/com/google/android/as/oss/feedback/domain:feedback_ui_state",
        "//src/com/google/android/as/oss/feedback/domain:feedback_view_model",
        "//src/com/google/android/as/oss/feedback/domain:ground_truth_data",
        "//src/com/google/android/as/oss/feedback/domain:view_feedback_data",
        "//src/com/google/android/as/oss/feedback/proto:entity_feedback_dialog_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/serviceclient:service_client",
        "//src/com/google/android/as/oss/logging/uiusage/api:usage_data_service_kt_proto_lite",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/animation",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/runtime/saveable",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/hilt/navigation_compose",
        "//third_party/java/androidx/lifecycle/runtime_compose",
        "//third_party/java/androidx/lifecycle/viewmodel_compose",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
    ],
)

android_library(
    name = "multi_entity_feedback_dialog",
    srcs = [
        "MultiEntityFeedbackDialog.kt",
        "MultiEntityFeedbackDialogV1.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":feedback_content_scaffold",
        ":feedback_opt_in_content",
        ":feedback_tag_chips",
        ":same_size_layout",
        ":view_feedback_data_dialog",
        "//src/com/google/android/as/oss/delegatedui/service/templates/fonts:flex_fonts",
        "//src/com/google/android/as/oss/delegatedui/service/templates/motion:expressive_motion",
        "//src/com/google/android/as/oss/feedback/domain:feedback_submission_data",
        "//src/com/google/android/as/oss/feedback/domain:feedback_ui_consts",
        "//src/com/google/android/as/oss/feedback/domain:feedback_ui_state",
        "//src/com/google/android/as/oss/feedback/domain:feedback_view_model",
        "//src/com/google/android/as/oss/feedback/domain:ground_truth_data",
        "//src/com/google/android/as/oss/feedback/domain:view_feedback_data",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:multi_feedback_dialog_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:quartz_log_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
        "//src/com/google/android/as/oss/logging/uiusage/api:usage_data_service_kt_proto_lite",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/animation",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/hilt/navigation_compose",
        "//third_party/java/androidx/lifecycle/viewmodel_compose",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
    ],
)

android_library(
    name = "view_feedback_data_dialog",
    srcs = [
        "ViewFeedbackDataDialog.kt",
        "ViewFeedbackDataDialogV1.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":scroll_child_into_view",
        "//src/com/google/android/as/oss/feedback/domain:feedback_ui_state",
        "//src/com/google/android/as/oss/feedback/domain:view_feedback_data",
        "//src/com/google/android/as/oss/feedback/quartz/serviceclient:quartz_service_client",
        "//src/com/google/android/as/oss/feedback/serviceclient:service_client",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/animation",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/runtime/saveable",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
    ],
)

android_library(
    name = "feedback_opt_in_content",
    srcs = [
        "FeedbackOptInContent.kt",
        "FeedbackOptInContentV1.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "//src/com/google/android/as/oss/delegatedui/service/templates/fonts:flex_fonts",
        "//src/com/google/android/as/oss/feedback/domain:feedback_ui_state",
        "//src/com/google/android/as/oss/feedback/domain:feedback_view_model",
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
    ],
)

android_library(
    name = "feedback_content_scaffold",
    srcs = ["FeedbackContentScaffold.kt"],
    deps = [
        ":fill_content_column_layout",
        ":same_size_layout",
        "//third_party/java/androidx/compose/animation",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/hilt/navigation_compose",
        "//third_party/java/androidx/lifecycle/viewmodel_compose",
    ],
)

android_library(
    name = "same_size_layout",
    srcs = ["SameSizeLayout.kt"],
    deps = [
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
    ],
)

android_library(
    name = "fill_content_column_layout",
    srcs = ["FillContentColumnLayout.kt"],
    deps = [
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
    ],
)

android_library(
    name = "scroll_child_into_view",
    srcs = ["ScrollChildIntoView.kt"],
    deps = [
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
    ],
)

android_library(
    name = "feedback_tag_chips",
    srcs = [
        "FeedbackTagChips.kt",
        "SingleEntityFeedbackTagChipsV1.kt",
    ],
    deps = [
        ":ground_truth_selector",
        "//src/com/google/android/as/oss/feedback/domain:ground_truth_data",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
    ],
)

android_library(
    name = "ground_truth_selector",
    srcs = ["GroundTruthSelector.kt"],
    deps = [
        "//src/com/google/android/as/oss/feedback/domain:ground_truth_data",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
    ],
)
