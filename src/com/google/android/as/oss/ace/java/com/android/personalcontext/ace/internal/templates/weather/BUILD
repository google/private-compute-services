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
    name = "weather_data",
    srcs = [
        "WeatherTemplateData.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/weather",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:theme_compat",
    ],
)

android_library(
    name = "weather",
    srcs = [
        "WeatherVisualizerTemplate.kt",
    ],
    deps = [
        ":weather_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/serversideclose",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/weather",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:modifier_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal:resources",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/findprototypehint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:theme_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/text",
        "//third_party/java/androidx/compose/ui/unit",
        "//third_party/java/androidx/core/ktx",
        "@maven//:androidx_core_core",
        "@maven//:javax_inject_javax_inject",
    ],
)
