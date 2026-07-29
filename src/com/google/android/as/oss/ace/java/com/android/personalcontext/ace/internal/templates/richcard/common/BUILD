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
    name = "common",
    srcs = [
        "CardActionRow.kt",
        "CardAppContextBlock.kt",
        "CardDismissIcon.kt",
        "CardEventReporter.kt",
        "CardInsight.kt",
        "CardTemplateLayout.kt",
        "CardTitle.kt",
        "CardTypography.kt",
        "LoadingComponents.kt",
        "ModifierUtils.kt",
        "RichCardTheme.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/serversideclose",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/energyeffects",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/flexfont",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:energy_effects_animation_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/utils:remote_action_utils",
        "//third_party/java/android/android_sdk_linux/platforms/stable:android_platform_feature_flags",
        "//third_party/java/androidx/compose/animation/animation_core",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/runtime/saveable",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/core/ktx",
        "@maven//:androidx_core_core",
    ],
)
