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
    name = "visualizer_service_connector",
    srcs = ["VisualizerServiceConnector.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
    ],
)

android_library(
    name = "visualizer_service_connector_impl",
    srcs = ["VisualizerServiceConnectorImpl.kt"],
    deps = [
        ":visualizer_service_connector",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/session:embedded_session_hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:meta_tags",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:pretty_print",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/wrappers",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:client_signal_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:embedded_scroll_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:empty_render_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:insight_event_reporter_factory_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:prototype_transform_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compose:compose_view_factory",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/embeddedscroll",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/embeddedtheme",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/session:visualizer_session",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/session:visualizer_session_factory",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//third_party/java/androidx/compose/animation/animation_core",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/foundation/layout",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/runtime/saveable",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/unit",
        "@maven//:com_google_dagger_dagger",
    ],
)
