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
load("//java/com/google/android/as/oss/asi:build_i18n.bzl", "LOCALES_SETTINGS", "TC_PROJECT_SMART_SUGGESTIONS")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl", "kt_jvm_lite_proto_library")
load("//tools/build_defs:tc.bzl", "TCExtractMessages", "TCGenerateLocalizedFiles")

package(default_visibility = [
    "//visibility:public",
])

proto_library(
    name = "super_icon_consent_proto",
    srcs = ["super_icon_consent.proto"],
    visibility = ["//visibility:private"],
    deps = [""],
)

kt_jvm_lite_proto_library(
    name = "super_icon_consent_kt_proto_lite",
    deps = [":super_icon_consent_proto"],
)

android_library(
    name = "conversation_content_connection",
    srcs = [
        "ConversationContentCallbackHelper.kt",
        "ConversationContentConnection.kt",
        "ConversationContentConnectionFactory.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    deps = [
        ":super_icon_consent_manager",
        "//java/com/google/android/apps/pixel/psi/service:ambient_data_parcelables",
        "//java/com/google/android/apps/pixel/psi/service/proto:ambient_data_service_kt_proto_lite",
        "//java/com/google/android/libraries/pixel/psi/ambientdata:ambient_data_client",
        "//java/com/google/common/time:time-android",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/supericon/aidl:conversation_content_service_aidl",
        "//src/com/google/android/as/oss/supericon/aidl:conversation_data",
        "//src/com/google/android/as/oss/supericon/aidl:super_icon_render_service_aidl",
        "//src/com/google/android/as/oss/supericon/config",
        "//src/com/google/android/as/oss/supericon/utils",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "super_icon_consent_manager",
    srcs = [
        "SuperIconConsentManager.kt",
        "SuperIconConsentModule.kt",
    ],
    deps = [
        ":super_icon_consent_kt_proto_lite",
        "//java/com/google/android/libraries/clock",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/supericon/config",
        "//third_party/java/androidx/datastore",
        "//third_party/java/androidx/datastore/core",
        "//third_party/java/androidx/datastore/core:proto_serializer-android",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "super_icon_render_service",
    srcs = [
        "SuperIconRenderService.kt",
        "SurfaceControlViewHostFactory.kt",
    ],
    exports_manifest = True,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]) + [":localized_resources"],
    deps = [
        ":conversation_content_connection",
        ":super_icon_consent_manager",
        "//googledata/icons/android/google_symbols/check:vd_theme_24",
        "//googledata/icons/android/google_symbols/clear:vd_theme_24",
        "//src/com/google/android/as/oss/common",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/supericon/aidl:conversation_data",
        "//src/com/google/android/as/oss/supericon/aidl:render_options",
        "//src/com/google/android/as/oss/supericon/aidl:super_icon_render_service_aidl",
        "//src/com/google/android/as/oss/supericon/config",
        "//src/com/google/android/as/oss/supericon/utils",
        "//third_party/java/android_libs/material_components:chip",
        "//third_party/java/android_libs/material_components:materialswitch",
        "//third_party/java/androidx/constraintlayout",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:com_google_android_material_material",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

LOCALIZATION_SRCS = [
    "res/values/strings.xml",
]

TCExtractMessages(
    name = "xmb_files",
    handler = "android",
    project = TC_PROJECT_SMART_SUGGESTIONS,
    source_dir = "java/com/google/android/apps/miphone/pcs/supericon/service",
    source_files = LOCALIZATION_SRCS,
)

TCGenerateLocalizedFiles(
    name = "localized_resources",
    srcs = LOCALIZATION_SRCS,
    handler = "android",
    locales = LOCALES_SETTINGS,
    options = {"localizedFileNameStyle": "android"},
    project = TC_PROJECT_SMART_SUGGESTIONS,
)
