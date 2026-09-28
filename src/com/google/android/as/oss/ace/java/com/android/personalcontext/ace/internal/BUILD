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
    name = "resources",
    custom_package = "com.android.personalcontext.ace.internal",
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/v7/appcompat",
    ],
)

android_library(
    name = "personal_context_templates_dagger_module",
    srcs = ["PersonalContextInternalTemplatesModule.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/gboard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:cards_decoders",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:rich_card_visualizer_template",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/weather",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "@maven//:com_google_dagger_dagger",
    ],
)

android_library(
    name = "personal_context_hilt_module",
    srcs = ["PersonalContextHiltModule.kt"],
    tags = ["keep_dep"],
    deps = [
        ":personal_context_dagger_compat",
        ":personal_context_templates_dagger_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer:personal_context_dagger_module",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer:personal_context_visualizer_templates_module",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "personal_context_dagger_compat",
    srcs = ["PersonalContextModuleCompat.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:card_insight_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:client_action_insight_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:client_signal_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:embedded_scroll_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:empty_render_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:energy_effects_animation_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:flex_font_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:insight_event_reporter_factory_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:insight_grid_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:prototype_transformation_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:theme_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/compat:visual_metadata_compat_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:card_insight_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:client_action_insight_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:client_signal_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:embedded_scroll_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:empty_render_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:energy_effects_animation_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:flex_font_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:insight_event_reporter_factory_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:insight_grid_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:prototype_transform_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:theme_compat",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compat:visual_metadata_compat",
        "@maven//:com_google_dagger_dagger",
    ],
)
