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
        "//third_party/java/android_app/personalcontext:external",
    ],
)

android_library(
    name = "prototype_utils",
    srcs = [
        "PrototypeHintUtils.kt",
        "PrototypeInsightUtils.kt",
    ],
    deps = [
        ":prototype_hint",
        ":prototype_insight",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/aacard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/blueflax:blueflax_metadata_hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/card",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientsignal",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/contact",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/crossdevice:intent",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/dialer:dialer_metadata_hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/embeddedscroll",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/empty",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/entitytype",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/example",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/gboard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/grid",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/loading",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/lookup",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/message:message_metadata_hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/metadata",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/rendertoken:hint",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/rendertoken:insight",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/republish",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/richcard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/serversideclose",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/theme",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/weather",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common/builders",
    ],
)

android_library(
    name = "prototype_hint",
    srcs = [
        "PrototypeHint.kt",
    ],
    deps = [
        "@maven//:androidx_annotation_annotation",
    ],
)

android_library(
    name = "prototype_insight",
    srcs = [
        "PrototypeInsight.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:labeled_context_insight",
        "@maven//:androidx_annotation_annotation",
    ],
)
