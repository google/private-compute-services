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
    name = "params",
    srcs = [
        "ClientActionParams.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/common:client_action_parameters",
        "@maven//:androidx_annotation_annotation",
    ],
)

android_library(
    name = "factory",
    srcs = [
        "ClientActionParamsFactory.kt",
    ],
    deps = [
        ":params",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params/blueflax",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params/fullscreenrequest",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params/hidekeyboard",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params/sharelivelocation",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params/sharephoto",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params/showcards",
        "//src/com/google/android/as/oss/ace/java/com/android/personalcontext/ace/client/prototype/clientaction/params/textpaste",
    ],
)
