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
    name = "attribution_data_visibility",
    includes = ["//visibility:public"],
)

android_library(
    name = "dataattribution",
    srcs = [
        "DataAttributionApi.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest_api.xml",
    visibility = [":attribution_data_visibility"],
    deps = [
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_chip_header_kt_proto_lite",
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_dialog_kt_proto_lite",
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
        ":data_attribution_activity",
    ],
)

android_library(
    name = "data_attribution_activity",
    srcs = [
        "DataAttributionActivity.kt",
        "DataAttributionActivityViewModel.kt",
        "DialogAnchor.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":data_attribution_components",
        ":dataattribution",
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_chip_header_kt_proto_lite",
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_dialog_kt_proto_lite",
        "//third_party/java/android_libs/material_components:dialog",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/runtime",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
    ],
)

android_library(
    name = "data_attribution_components",
    srcs = ["DataAttributionDialog.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_chip_header_kt_proto_lite",
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_dialog_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/templates/fonts:flex_fonts",
        "//src/com/google/android/as/oss/delegatedui/utils:serializable_bitmap",
        "//src/com/google/android/as/oss/delegatedui/utils:tintable_icon",
        "//src/com/google/android/as/oss/feedback",
        "//src/com/google/android/as/oss/feedback/proto:entity_feedback_dialog_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//third_party/java/android/android_sdk_linux/platforms/stable:android_platform_feature_flags",
        "//third_party/java/android_libs/material_components:dialog",
        "//third_party/java/androidx/activity/compose",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
    ],
)
