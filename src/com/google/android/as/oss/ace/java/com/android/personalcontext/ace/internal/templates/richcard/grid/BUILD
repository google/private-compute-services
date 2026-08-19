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
    default_applicable_licenses = ["//third_party/java/android_app/personalcontext:license"],
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "grid_card_ui_data",
    srcs = ["GridCardUiData.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:insight_grid_item",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_type",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
    ],
)

android_library(
    name = "decoder",
    srcs = ["GridCardUiDataDecoder.kt"],
    deps = [
        ":grid_card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/card",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/grid",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/metadata",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/metadata:visual_style",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:displayable_context_insight",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/findprototypehint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/decoder:card_ui_data_decoder",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "decoder_module",
    srcs = ["GridCardUiDataDecoderModule.kt"],
    deps = [
        ":decoder",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_type",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/decoder:card_ui_data_decoder",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "renderer",
    srcs = ["GridCardRenderer.kt"],
    deps = [
        ":grid_card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/grid",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/common",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/renderer",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:energy_effects_animation_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/ui/common/grid:justified_wrap_layout",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "renderer_module",
    srcs = ["GridCardRendererModule.kt"],
    deps = [
        ":renderer",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_type",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/renderer",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
