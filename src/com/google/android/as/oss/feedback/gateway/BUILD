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
    name = "feedback_http_client",
    srcs = ["FeedbackHttpClient.kt"],
    deps = ["//src/com/google/android/as/oss/feedback/proto/gateway:pixel_apex_api_kt_proto_lite"],
)

android_library(
    name = "feedback_http_client_impl",
    srcs = [
        "FeedbackHttpClientImpl.kt",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        ":feedback_http_client",
        ":http_client_helper",
        "//java/com/google/common/annotations",
        "//src/com/google/android/as/oss/feedback/messagearmour/utils:message_armour_data_helper",
        "//src/com/google/android/as/oss/feedback/proto/gateway:message_armour_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:pixel_apex_api_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:quartz_log_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/quartz/utils:quartz_data_helper",
        "//src/com/google/android/as/oss/networkusage/config/impl",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_squareup_okhttp3_okhttp",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "http_client_helper",
    srcs = ["FeedbackHttpClientHelper.kt"],
    deps = ["@maven//:com_google_flogger_google_extensions"],
)

android_library(
    name = "feedback_http_client_module",
    srcs = ["FeedbackHttpClientModule.kt"],
    deps = [
        ":feedback_http_client",
        ":feedback_http_client_impl",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)
