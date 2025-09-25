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
    name = "annotations",
    srcs = ["Annotations.kt"],
    deps = ["@maven//:javax_inject_javax_inject"],
)

android_library(
    name = "quartz_annotations",
    srcs = ["QuartzAnnotations.kt"],
    deps = ["@maven//:javax_inject_javax_inject"],
)

android_library(
    name = "module",
    srcs = ["ServiceConnectionModule.kt"],
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    deps = [
        ":annotations",
        ":quartz_annotations",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/security:security_policy_utils",
        "//src/com/google/android/as/oss/common/security/config",
        "//src/com/google/android/as/oss/feedback/proto/dataservice:data_service_kt_grpc",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:javax_inject_javax_inject",
    ],
)
