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
    name = "conversation_id_update_service",
    srcs = ["ConversationIdUpdateService.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    deps = [
        "//java/com/google/android/apps/common/inject/annotation",
        "//java/com/google/common/time:time-android",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/conversationid/config",
        "//src/com/google/android/as/oss/conversationid/service/aidl:update_conversation_id_service_aidl",
        "//src/com/google/android/as/oss/conversationid/util:conversation_id_manager",
        "//src/com/google/android/as/oss/conversationid/util:service_validator",
        "//src/com/google/android/as/oss/conversationid/util:util_module",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "conversation_id_listener_service",
    srcs = ["ConversationIdListenerService.kt"],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    deps = [
        "//java/com/google/android/apps/common/inject/annotation",
        "//java/com/google/common/time:time-android",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/conversationid/config",
        "//src/com/google/android/as/oss/conversationid/service/aidl:conversation_id_listener_service_aidl",
        "//src/com/google/android/as/oss/conversationid/util:conversation_id_manager",
        "//src/com/google/android/as/oss/conversationid/util:service_validator",
        "//src/com/google/android/as/oss/conversationid/util:util_module",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject",
    ],
)
