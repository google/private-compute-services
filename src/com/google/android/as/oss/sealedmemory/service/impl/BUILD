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
    name = "impl",
    srcs = [
        "SealedMemorySecretAccessService.kt",
        "SealedMemorySecretManagementService.kt",
    ],
    deps = [
        ":app_search_key_store",
        ":encrypted_key",
        ":encryption_result",
        ":key_generation_util",
        "//src/com/google/android/as/oss/sealedmemory/api:service_kt_grpc_lite",
        "//src/com/google/android/as/oss/sealedmemory/api:service_kt_proto_lite",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "encrypted_key",
    srcs = ["EncryptedKey.kt"],
    deps = ["@maven//:com_google_protobuf_protobuf_javalite"],
)

android_library(
    name = "key_generation_util",
    srcs = ["KeyGenerationUtil.kt"],
    deps = [
        ":encrypted_key",
        ":encryption_result",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

android_library(
    name = "app_search_session_provider",
    srcs = ["AppSearchSessionProvider.kt"],
    deps = [
        "//third_party/java/androidx/appsearch",
    ],
)

android_library(
    name = "platform_app_search_session_provider",
    srcs = ["PlatformAppSearchSessionProvider.kt"],
    deps = [
        ":app_search_session_provider",
        "//third_party/java/androidx/appsearch",
        "//third_party/java/androidx/appsearch/platform_storage",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines_guava-android",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "encryption_result",
    srcs = ["EncryptionResult.kt"],
)

android_library(
    name = "key_association",
    srcs = ["KeyAssociation.kt"],
    deps = [
        "//third_party/java/androidx/appsearch",
    ],
)

android_library(
    name = "encrypted_key_document",
    srcs = ["EncryptedKeyDocument.kt"],
    deps = [
        "//third_party/java/androidx/appsearch",
    ],
)

android_library(
    name = "app_search_key_store",
    srcs = ["AppSearchKeyStore.kt"],
    deps = [
        ":app_search_session_provider",
        ":encrypted_key",
        ":encrypted_key_document",
        ":key_association",
        "//java/com/google/async/coroutines:annotations",
        "//third_party/java/androidx/appsearch",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines_guava-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "app_search_module",
    srcs = ["AppSearchModule.kt"],
    visibility = ["//visibility:public"],
    deps = [
        ":app_search_session_provider",
        ":platform_app_search_session_provider",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)
