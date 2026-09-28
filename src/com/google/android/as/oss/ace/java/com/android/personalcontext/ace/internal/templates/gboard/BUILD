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
    name = "gboard",
    srcs = [
        "GboardVisualizerTemplate.kt",
    ],
    deps = [
        "//googledata/icons/android/google_symbols/chrome_product:vd_theme_24",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/gboard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:modifier_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/energyeffects",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/findprototypehint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/common",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:energy_effects_animation_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:theme_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/utils:embedded_theme",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/utils:tintable_icon",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/core/ktx",
        "@maven//:androidx_core_core",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)
