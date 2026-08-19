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

package(default_visibility = [
    "//java/com/google/android/apps/pixel/psi:libraries",
    "//visibility:public",
])

android_library(
    name = "card_type",
    srcs = ["CardType.kt"],
    deps = [
        "//src/com/google/android/as/oss/logging:psi_enums_kt_proto_lite",
        "@maven//:com_google_dagger_dagger",
    ],
)

android_library(
    name = "card_ui_data",
    srcs = ["CardUiData.kt"],
    deps = [
        ":card_type",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/serversideclose",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:displayable_context_insight",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/core/ktx",
        "@maven//:androidx_core_core",
    ],
)

android_library(
    name = "deprecated_simple_card_context",
    srcs = ["DeprecatedUiSimpleCardContext.kt"],
    deps = [
        ":card_type",
        ":card_ui_data",
    ],
)

android_library(
    name = "deprecated_grid_card_context",
    srcs = ["DeprecatedUiGridCardContext.kt"],
    deps = [
        ":card_type",
        ":card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:insight_grid_item",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/builders",
    ],
)

android_library(
    name = "rich_card_visualizer_template",
    srcs = ["RichCardVisualizerTemplate.kt"],
    deps = [
        ":card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/card",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/richcard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/findprototypehint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/common",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/decoder:card_decoder_manager",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/renderer",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "cards_decoders",
    exports = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/calendar:decoder_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/decoder:card_decoder_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/flight:decoder_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/grid:decoder_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/imagegallery:decoder_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/loading:decoder_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/locationpreview:decoder_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/placeholder:decoder_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/stackcard:decoder_module",
    ],
)

android_library(
    name = "cards_renderers",
    exports = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/calendar:renderer_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/flight:renderer_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/grid:renderer_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/imagegallery:renderer_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/loading:renderer_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/locationpreview:renderer_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/placeholder:renderer_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/renderer:card_renderer_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/stackcard:renderer_module",
    ],
)

android_library(
    name = "richcard",
    exports = [
        ":cards_renderers",
        ":rich_card_visualizer_template",
    ],
)
