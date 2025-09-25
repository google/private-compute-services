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

package(default_visibility = ["//visibility:public"])

android_library(
    name = "renderer",
    srcs = [
        "DelegatedUiRenderer.kt",
        "DelegatedUiViewParent.kt",
        "DelegatedUiViewRoot.kt",
        "ScrollVelocityTracker.kt",
    ],
    deps = [
        "//java/com/google/common/flogger/android:android_log_tag",
        "//src/com/google/android/as/oss/delegatedui/api/common:nested_scroll",
        "//src/com/google/android/as/oss/delegatedui/api/infra/dataservice:data_service_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress:egress_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:templates_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/common",
        "//src/com/google/android/as/oss/delegatedui/service/data",
        "//src/com/google/android/as/oss/delegatedui/service/templates",
        "//src/com/google/android/as/oss/delegatedui/service/templates/airflow:module",
        "//src/com/google/android/as/oss/delegatedui/service/templates/beacon:module",
        "//src/com/google/android/as/oss/delegatedui/service/templates/bugle:module",
        "//src/com/google/android/as/oss/delegatedui/service/templates/subzero:module",
        "//src/com/google/android/as/oss/delegatedui/service/templates/sundog:module",
        "//third_party/java/androidx/lifecycle",
        "//third_party/java/androidx/lifecycle/viewmodel",
        "//third_party/java/androidx/savedstate",
        "@maven//:androidx_core_core",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "module",
    srcs = [
        "DelegatedUiRendererModule.kt",
    ],
    deps = [
        ":renderer",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
