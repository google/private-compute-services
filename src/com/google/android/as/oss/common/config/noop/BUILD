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

android_library(
    name = "flags_impl_noop",
    srcs = [
        "DeviceFlagManagerFactoryNoOp.java",
        "FlagManagerModuleNoOp.java",
    ],
    deps = [
        ":flag_manager_impl_noop",
        "//src/com/google/android/as/oss/common/config",
        "//third_party/java/hilt:hilt-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "flag_manager_impl_noop",
    srcs = ["DeviceFlagManagerNoOp.java"],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "@maven//:org_checkerframework_checker_qual",
    ],
)
