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
    name = "call",
    srcs = [
        "CallVisualizerTemplate.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:find_hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:flex_font_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:insight_grid_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:visual_metadata_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/call/compose:call_widget",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/call/data:call_template_data",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/hilt/navigation_compose",
        "@maven//:javax_inject_javax_inject",
    ],
)
