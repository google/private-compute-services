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

package(default_visibility = [
    "//java/com/google/android/apps/pixel/psi:libraries",
    "//visibility:public",
])

android_library(
    name = "card_ui_data_decoder",
    srcs = ["CardUiDataDecoder.kt"],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/card",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/loading",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/richcard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/serversideclose",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:displayable_context_insight",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/findprototypehint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard/common/utils:card_action_utils",
        "//third_party/java/androidx/compose/foundation/layout",
    ],
)

android_library(
    name = "card_decoder_manager",
    srcs = ["CardDecoderManager.kt"],
    deps = [
        ":card_ui_data_decoder",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/card",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_type",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "card_decoder_module",
    srcs = ["CardDecoderModule.kt"],
    deps = [
        ":card_ui_data_decoder",
        ":grid_card_ui_data_decoder",
        ":simple_card_ui_data_decoder",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_type",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:card_ui_data",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "simple_card_ui_data_decoder",
    srcs = ["SimpleCardUiDataDecoder.kt"],
    deps = [
        ":card_ui_data_decoder",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:deprecated_simple_card_context",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "grid_card_ui_data_decoder",
    srcs = ["GridCardUiDataDecoder.kt"],
    deps = [
        ":card_ui_data_decoder",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype:prototype_utils",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/grid",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/internal/templates/richcard:deprecated_grid_card_context",
        "@maven//:javax_inject_javax_inject",
    ],
)
