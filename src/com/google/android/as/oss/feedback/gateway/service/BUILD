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
    name = "service",
    srcs = ["FeedbackHttpClientService.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    deps = [
        "//java/com/google/android/apps/common/inject/annotation",
        "//src/com/google/android/as/oss/common",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/security:security_policy_utils",
        "//src/com/google/android/as/oss/common/security/api:package_security_info_java_proto_lite",
        "//src/com/google/android/as/oss/common/security/config",
        "//src/com/google/android/as/oss/feedback/gateway:feedback_http_client",
        "//src/com/google/android/as/oss/feedback/gateway/service/aidl",
        "//src/com/google/android/as/oss/feedback/proto/gateway:pixel_apex_api_kt_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:javax_inject_javax_inject",
    ],
)
