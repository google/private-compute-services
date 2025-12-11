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
    name = "sideeffects",
    srcs = [
        "CloseSessionSideEffect.kt",
        "DismissSuggestionSideEffect.kt",
        "ExecuteActionSideEffect.kt",
        "LogUsageSideEffect.kt",
        "SendEgressDataSideEffect.kt",
        "SendHintsSideEffect.kt",
        "ShowDataAttributionSideEffect.kt",
        "ShowFeedbackSideEffect.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/dataattribution",
        "//src/com/google/android/as/oss/dataattribution:data_attribution_activity",
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_chip_header_kt_proto_lite",
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_dialog_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/common:common_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/infra/dataservice:data_service_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress:egress_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/templates/scope:helpers",
        "//src/com/google/android/as/oss/feedback",
        "//src/com/google/android/as/oss/feedback:feedback_activity",
        "//src/com/google/android/as/oss/feedback/proto:entity_feedback_dialog_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:multi_feedback_dialog_data_kt_proto_lite",
        "//third_party/java/android/android_sdk_linux/platforms/stable:android_platform_feature_flags",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/core/ktx",
        "@maven//:com_google_flogger_google_extensions",
    ],
)
