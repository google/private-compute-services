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
    name = "message_template_data",
    srcs = [
        "MessageTemplateData.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/message:message_metadata_hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:client_action_insight_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:theme_compat",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/core/ktx",
    ],
)

android_library(
    name = "message_constants",
    srcs = ["MessageConstants.kt"],
    deps = [
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/unit",
    ],
)

android_library(
    name = "message_chip_animation",
    srcs = ["MessageChipAnimation.kt"],
    deps = [
        ":message_constants",
        "//third_party/java/androidx/compose/animation/animation_core",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:androidx_core_core",
    ],
)

android_library(
    name = "message",
    srcs = [
        "MessageVisualizerTemplate.kt",
    ],
    deps = [
        ":message_chip_animation",
        ":message_constants",
        ":message_template_data",
        "//googledata/icons/android/google_symbols/star:vd_theme_24",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params/showcards",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/message:message_metadata_hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/serversideclose",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:find_hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:modifier_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/energyeffects",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:client_action_insight_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:energy_effects_animation_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:flex_font_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:theme_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/utils:remote_action_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/utils:tintable_icon",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/delegatedui/config",
        "//src/com/google/android/as/oss/delegatedui/service/templates/motion:expressive_motion",
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
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:androidx_core_core",
        "@maven//:javax_inject_javax_inject",
    ],
)
