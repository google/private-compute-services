# Copyright 2024 Google LLC
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

package(default_visibility = ["//visibility:public"])

android_library(
    name = "time",
    srcs = [
        "TimeSource.java",
    ],
    deps = [
    ],
)

android_library(
    name = "system_module",
    srcs = [
        "SystemTimeSourceModule.java",
    ],
    deps = [
        ":time",
        "//third_party/java/hilt:hilt-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject",
    ],
)
