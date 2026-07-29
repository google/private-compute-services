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
    name = "call_widget_constants",
    srcs = [
        "CallWidgetConstants.kt",
    ],
    deps = [
        "//third_party/java/androidx/compose/ui/unit",
    ],
)

android_library(
    name = "common",
    srcs = [
        "AiDisclaimer.kt",
        "CallFeedback.kt",
        "DeeplinkText.kt",
    ],
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer:resources",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/utils:remote_action_utils",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material/icons_extended",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/runtime/saveable",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
    ],
)

android_library(
    name = "call_widget",
    srcs = [
        "CallDetailedCardContainer.kt",
        "CallGeneralCardsContainer.kt",
        "CallTemplate.kt",
        "CallWidgetContainer.kt",
        "FullScreenCallWidget.kt",
    ],
    visibility = [
        "//javatests/com/google/android/apps/miphone/pcs/ace/java/com/android/personalcontext/ace/visualizer/templates/call:__subpackages__",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/call:__subpackages__",
    ],
    deps = [
        ":call_providers",
        ":call_widget_constants",
        ":common",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer:resources",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:flex_font_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:insight_grid_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/call/data:call_template_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/ui/common/grid:justified_wrap_layout",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/utils:tintable_icon",
        "//third_party/java/androidx/compose/animation",
        "//third_party/java/androidx/compose/animation/animation_core",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material/icons_core",
        "//third_party/java/androidx/compose/material/icons_extended",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/runtime/saveable",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/core/ktx",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_core_core",
    ],
)

android_library(
    name = "call_providers",
    srcs = [
        "CallProviders.kt",
    ],
    visibility = [
        "//javatests/com/google/android/apps/miphone/pcs/ace/java/com/android/personalcontext/ace/visualizer/templates/call:__subpackages__",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/call:__subpackages__",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:insight_grid_compat",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui/graphics",
    ],
)
