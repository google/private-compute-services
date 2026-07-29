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
    name = "embedded_scroll_compat_impl",
    srcs = ["EmbeddedScrollCompatImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/embeddedscroll",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:embedded_scroll",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:embedded_scroll_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "empty_render_compat_impl",
    srcs = ["EmptyRenderCompatImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/empty",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:empty_render_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "flex_font_compat_impl",
    srcs = ["FlexFontCompatImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/flexfont",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:flex_font_compat",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/ui/text",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "insight_event_reporter_factory_compat_impl",
    srcs = ["InsightEventReporterFactoryCompatImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:flat_index",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:meta_tags",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:packed_int",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:insight_event_reporter_factory_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/utils/trafficshaperqueue:traffic_shaper_queue",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "prototype_transformation_compat_impl",
    srcs = ["PrototypeTransformCompatImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:labeled_context_insight",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:prototype_transform_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "card_insight_compat_impl",
    srcs = ["CardInsightCompatImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/card",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:card_insight_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "insight_grid_compat_impl",
    srcs = ["InsightGridCompatImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/grid",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:insight_grid_item",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:insight_grid_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "client_signal_compat_impl",
    srcs = ["ClientSignalCompatImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientsignal",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:client_signal_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "client_action_insight_compat_impl",
    srcs = ["ClientActionInsightCompatImpl.kt"],
    manifest = "AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:insight_extended_details",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:client_action_insight_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "theme_compat_impl",
    srcs = ["ThemeCompatImpl.kt"],
    manifest = "AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/theme",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:theme_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "energy_effects_animation_compat_impl",
    srcs = [
        "EnergyEffectsAnimationCompatImpl.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/energyeffects",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:energy_effects_animation_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:theme_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//third_party/java/androidx/compose/animation/animation_core",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/unit",
        "@maven//:androidx_core_core",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "visual_metadata_compat_impl",
    srcs = ["VisualMetadataCompatImpl.kt"],
    manifest = "AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/metadata",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/metadata:visual_style",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:visual_metadata_compat",
        "@maven//:javax_inject_javax_inject",
    ],
)
