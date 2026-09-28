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
        "//third_party/java/android_app/personalcontext:libraries",
    ],
)

android_library(
    name = "embedded_scroll_compat",
    srcs = ["EmbeddedScrollCompat.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:embedded_scroll",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
    ],
)

android_library(
    name = "empty_render_compat",
    srcs = ["EmptyRenderCompat.kt"],
    deps = [],
)

android_library(
    name = "prototype_transform_compat",
    srcs = ["PrototypeTransformCompat.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:labeled_context_insight",
    ],
)

android_library(
    name = "flex_font_compat",
    srcs = ["FlexFontCompat.kt"],
    deps = [
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/ui/text",
    ],
)

android_library(
    name = "insight_event_reporter_factory_compat",
    srcs = ["InsightEventReporterFactoryCompat.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
    ],
)

android_library(
    name = "card_insight_compat",
    srcs = ["CardInsightCompat.kt"],
    deps = [
    ],
)

android_library(
    name = "insight_grid_compat",
    srcs = ["InsightGridCompat.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:insight_grid_item",
    ],
)

android_library(
    name = "client_signal_compat",
    srcs = ["ClientSignalCompat.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
    ],
)

android_library(
    name = "client_action_insight_compat",
    srcs = ["ClientActionInsightCompat.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:client_action_parameters",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:insight_extended_details",
    ],
)

android_library(
    name = "visual_metadata_compat",
    srcs = ["VisualMetadataCompat.kt"],
    deps = [],
)

android_library(
    name = "theme_compat",
    srcs = ["ThemeCompat.kt"],
    deps = [],
)

android_library(
    name = "energy_effects_animation_compat",
    srcs = [
        "EnergyEffectsAnimationCompat.kt",
        "EnergyEffectsAnimationSpec.kt",
    ],
    deps = [
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/unit",
    ],
)
