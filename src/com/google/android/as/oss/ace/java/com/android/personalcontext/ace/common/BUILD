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

package(
    default_visibility = [
        "//third_party/java/android_app/personalcontext:external",
    ],
)

android_library(
    name = "pretty_print",
    srcs = ["PrettyPrintUtils.kt"],
    deps = [
        ":render_token",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:labeled_context_insight",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
    ],
)

android_library(
    name = "render_token",
    srcs = ["RenderTokenUtils.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
    ],
)

android_library(
    name = "find_hint",
    srcs = ["FindHintUtils.kt"],
    deps = [],
)

android_library(
    name = "packed_color_utils",
    srcs = ["PackedColorUtils.kt"],
    deps = [
        "//third_party/java/androidx/compose/ui/graphics",
    ],
)

android_library(
    name = "modifier_utils",
    srcs = ["ModifierUtils.kt"],
    visibility = [
        "//javatests/com/google/android/apps/miphone/pcs:__subpackages__",
        "//src/com/google/android/as/oss:__subpackages__",
    ],
    deps = [
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
    ],
)

android_library(
    name = "embedded_scroll",
    srcs = [
        "EmbeddedScrollEvent.kt",
    ],
    deps = [
        "@maven//:androidx_core_core",
    ],
)

android_library(
    name = "energy_color_utils",
    srcs = ["EnergyColorUtils.kt"],
    visibility = [
        "//src/com/google/android/as/oss:__subpackages__",
    ],
    deps = [
        "//java/com/google/android/systemui/graphics/shaderlib/ane/colors",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "@maven//:androidx_annotation_annotation",
    ],
)

android_library(
    name = "flat_index",
    srcs = [
        "FlatIndexUtils.kt",
    ],
    deps = ["//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers"],
)

android_library(
    name = "packed_int",
    srcs = [
        "PackedIntUtils.kt",
    ],
    deps = [],
)

android_library(
    name = "insight_grid_item",
    srcs = [
        "InsightGridItem.kt",
    ],
    deps = [],
)

android_library(
    name = "personal_context_manager_helper",
    srcs = ["PersonalContextManagerHelper.kt"],
    visibility = [
        "//java/com/google/android/libraries/pixel/psi:__subpackages__",
        "//third_party/java/android_app/personalcontext:external",
    ],
    deps = [
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "meta_tags",
    srcs = [
        "MetaTags.kt",
    ],
    deps = [],
)

android_library(
    name = "labeled_context_insight",
    srcs = [
        "LabeledContextInsight.kt",
    ],
    deps = [],
)

android_library(
    name = "client_action_parameters",
    srcs = ["ClientActionParameters.kt"],
)

android_library(
    name = "displayable_context_insight",
    srcs = [
        "DisplayableContextInsight.kt",
    ],
    deps = [],
)

android_library(
    name = "insight_extended_details",
    srcs = ["InsightExtendedDetails.kt"],
)
