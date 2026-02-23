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

load("@bazel_rules_android//android:rules.bzl", "android_library", "kt_jvm_library_with_nullness_check")
load("//mobile/build:build_defs.bzl", "android_jni_library")
load("//third_party/bazel_rules/rules_cc/cc:cc_binary.bzl", "cc_binary")
load("//third_party/bazel_rules/rules_cc/cc:cc_library.bzl", "cc_library")

# This list should remain very restricted to bsa/jni.
package(default_visibility = [
    "//javatests/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni:__subpackages__",
    "//src/com/google/android/as/oss/privateinference/library/bsa/jni:__subpackages__",
])

android_library(
    name = "blind_sign_auth_jni_bridge",
    srcs = ["BlindSignAuthJniBridge.kt"],
    # This list should remain restricted to bsa.
    visibility = [
        "//javatests/com/google/android/apps/miphone/pcs/privateinference/library/bsa/impl:__subpackages__",
        "//src/com/google/android/as/oss/privateinference/library/bsa/impl:__subpackages__",
    ],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines_guava",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_api",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "blind_sign_auth_jni_bridge_jvm",
    srcs = ["BlindSignAuthJniBridge.kt"],
    visibility = [
        "//javatests/com/google/android/apps/miphone/pcs/privateinference/library/bsa/impl:__subpackages__",
        "//src/com/google/android/as/oss/privateinference/library/bsa/impl:__subpackages__",
    ],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth_jvm",
        "//third_party/java/flogger",
        "//third_party/java/grpc:core",
        "//third_party/java/protobuf",
        "//third_party/kotlin/kotlinx_coroutines",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines_guava-jvm",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_guava_guava",
    ],
)

cc_library(
    name = "jni_helpers",
    srcs = ["jni_helpers.cc"],
    hdrs = ["jni_helpers.h"],
    deps = [
        ":jni_logging",
        "//third_party/absl/functional:any_invocable",
        "//third_party/absl/status",
        "//third_party/absl/status:statusor",
        "//third_party/absl/strings",
        "//third_party/absl/time",
        "//third_party/absl/types:span",
        "//third_party/java/jdk:jni",
    ],
)

cc_library(
    name = "jni_bsa_calls",
    srcs = ["jni_bsa_calls.cc"],
    hdrs = [
        "jni_bsa_calls.h",
    ],
    deps = [
        ":jni_global_ref",
        ":jni_helpers",
        ":jni_logging",
        ":jni_message_interface_wrapper",
        "//third_party/absl/functional:any_invocable",
        "//third_party/absl/status",
        "//third_party/absl/status:statusor",
        "//third_party/absl/strings",
        "//third_party/absl/strings:str_format",
        "//third_party/absl/time",
        "//third_party/absl/types:span",
        "//third_party/java/jdk:jni",
        "//third_party/quiche/blind_sign_auth",
        "//third_party/quiche/blind_sign_auth:blind_sign_auth_interface",
        "//third_party/quiche/blind_sign_auth:blind_sign_message_interface",
        "//third_party/quiche/blind_sign_auth:blind_sign_message_response",
    ],
)

cc_library(
    name = "jni_message_interface_wrapper",
    srcs = ["jni_message_interface_wrapper.cc"],
    hdrs = ["jni_message_interface_wrapper.h"],
    deps = [
        ":jni_global_ref",
        ":jni_helpers",
        ":jni_logging",
        "//third_party/absl/cleanup",
        "//third_party/absl/functional:any_invocable",
        "//third_party/absl/status",
        "//third_party/absl/status:statusor",
        "//third_party/absl/strings",
        "//third_party/java/jdk:jni",
        "//third_party/quiche/blind_sign_auth:blind_sign_message_interface",
    ],
)

cc_library(
    name = "jni_global_ref",
    hdrs = ["jni_global_ref.h"],
    deps = [
        ":jni_helpers",
        ":jni_logging",
        "//third_party/absl/status:statusor",
        "//third_party/java/jdk:jni",
    ],
)

cc_library(
    name = "jni_logging",
    hdrs = ["jni_logging.h"],
    deps = [
        "//third_party/absl/strings:str_format",
    ],
)

cc_library(
    name = "blind_sign_auth_jni_lib",
    srcs = ["blind_sign_auth_jni.cc"],
    visibility = ["//src/com/google/android/as/oss/privateinference/library/oakutil/jni:__subpackages__"],
    deps = [
        ":jni_bsa_calls",
        ":jni_helpers",
        ":jni_logging",
        ":jni_message_interface_wrapper",
        "//third_party/absl/status",
        "//third_party/absl/strings",
        "//third_party/java/jdk:jni",
    ],
)

android_jni_library(
    name = "blind_sign_auth_jni",
    visibility = ["//javatests/com/google/android/apps/miphone/pcs/privateinference/arateaipblindingtestclient:__subpackages__"],
    deps = [
        ":blind_sign_auth_jni_lib",
    ],
)

cc_binary(
    name = "blind_sign_auth_jni_jvm",
    testonly = 1,
    linkshared = 1,
    linkstatic = 1,
    # For CLI tool
    visibility = [
        "//javatests/com/google/android/apps/miphone/pcs/privateinference/library/bsa:__subpackages__",
        "//src/com/google/android/as/oss/privateinference/library/bsa:__subpackages__",
    ],
    deps = [
        ":blind_sign_auth_jni_lib",
    ],
)

# Make the BUILD file visible for open-source packaging.
exports_files(["BUILD"])
