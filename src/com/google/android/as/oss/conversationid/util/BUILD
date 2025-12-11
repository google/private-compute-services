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
    "//visibility:public",
])

android_library(
    name = "hasher",
    srcs = ["ConversationIdHasher.kt"],
)

android_library(
    name = "conversation_id_holder",
    srcs = ["ConversationIdHolder.kt"],
    deps = [
        ":hasher",
        "//java/com/google/common/time:time-android",
    ],
)

android_library(
    name = "service_validator",
    srcs = ["ServiceValidator.kt"],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/security:security_policy_utils",
        "//src/com/google/android/as/oss/common/security/api:package_security_info_java_proto_lite",
        "//src/com/google/android/as/oss/common/security/config",
        "//src/com/google/android/as/oss/conversationid/config",
    ],
)

android_library(
    name = "conversation_id_manager",
    srcs = [
        "ConversationIdManager.kt",
    ],
    deps = [
        ":conversation_id_holder",
        "//java/com/google/common/time:time-android",
        "//java/com/google/common/time/sting",
        "//src/com/google/android/as/oss/conversationid/service/aidl:conversation_id_listener_service_aidl",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_flogger_google_extensions",
    ],
)

android_library(
    name = "util_module",
    srcs = [
        "UtilModule.kt",
    ],
    deps = [
        ":conversation_id_holder",
        ":conversation_id_manager",
        ":service_validator",
        "//java/com/google/common/time:time-android",
        "//java/com/google/common/time/sting",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/security/config",
        "//src/com/google/android/as/oss/conversationid/config",
        "//src/com/google/android/as/oss/conversationid/config/impl",  # unuseddeps: keep
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)
