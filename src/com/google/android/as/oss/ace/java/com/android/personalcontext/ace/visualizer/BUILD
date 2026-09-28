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
load("//tools/build_defs:tc.bzl", "TCExtractMessages", "TCGenerateLocalizedFiles")

package(
    default_visibility = [
        "//third_party/java/android_app/personalcontext:libraries",
    ],
)

android_library(
    name = "resources",
    custom_package = "com.android.personalcontext.ace.visualizer",
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]) + [":localized_resources"],
    deps = [
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/v7/appcompat",
    ],
)

# TODO: Clean up temporary l10n localization build rules once CardInsight migration lands in 26Q4.
TCExtractMessages(
    name = "xmb_files",
    handler = "android",
    project = TC_PROJECT_SMART_SUGGESTIONS,
    source_dir = "java/com/google/android/apps/miphone/pcs/ace/java/com/android/personalcontext/ace/visualizer",
    source_files = ["res/values/strings.xml"],
)

TCGenerateLocalizedFiles(
    name = "localized_resources",
    srcs = ["res/values/strings.xml"],
    handler = "android",
    locales = LOCALES_SETTINGS,
    options = {"localizedFileNameStyle": "android"},
    project = TC_PROJECT_SMART_SUGGESTIONS,
)

android_library(
    name = "personal_context_dagger_module",
    srcs = ["PersonalContextModule.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compose:compose_view_factory",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/compose:compose_view_factory_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/connector:visualizer_service_connector",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/connector:visualizer_service_connector_impl",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/session:visualizer_session_factory",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/session:visualizer_session_factory_impl",
        "@maven//:com_google_dagger_dagger",
    ],
)

android_library(
    name = "personal_context_visualizer_templates_module",
    srcs = ["PersonalContextVisualizerTemplatesModule.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/call",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/visualizer/templates/message",
        "@maven//:com_google_dagger_dagger",
    ],
)
