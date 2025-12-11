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
    name = "bugle",
    srcs = [
        "BugleAnimatedListItemVisibility.kt",
        "BugleSuggestionModel.kt",
        "BugleTemplateRenderer.kt",
        "Constants.kt",
    ],
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    visibility = ["//visibility:private"],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/dataattribution/proto:attribution_chip_header_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/common:common_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/infra/dataservice:data_service_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress/bugle:bugle_egress_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:templates_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:ui_id_token_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates/bugle:bugle_template_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/config",
        "//src/com/google/android/as/oss/delegatedui/service/common",
        "//src/com/google/android/as/oss/delegatedui/service/templates",
        "//src/com/google/android/as/oss/delegatedui/service/templates/fonts:flex_fonts",
        "//src/com/google/android/as/oss/delegatedui/service/templates/scope",
        "//src/com/google/android/as/oss/delegatedui/service/templates/scope/sideeffects",
        "//src/com/google/android/as/oss/delegatedui/utils:response_with_parcelables",
        "//src/com/google/android/as/oss/delegatedui/utils:serializable_bitmap",
        "//src/com/google/android/as/oss/delegatedui/utils:tintable_icon",
        "//third_party/java/android_libs/accompanist_drawablepainter",
        "//third_party/java/androidx/compose/animation",
        "//third_party/java/androidx/compose/animation/animation_core",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/runtime/saveable",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/core/ktx",
        "//third_party/java/androidx/lifecycle/viewmodel_compose",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:androidx_core_core",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "module",
    srcs = [
        "BugleTemplateHandlerModule.kt",
    ],
    deps = [
        ":bugle",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:templates_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/templates",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
