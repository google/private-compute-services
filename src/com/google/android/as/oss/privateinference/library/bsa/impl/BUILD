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

package(default_visibility = [
    "//java/com/google/android/libraries/mdi/privatearatea:__subpackages__",
    "//javatests/com/google/android/apps/miphone/pcs/privateinference/library/bsa:__subpackages__",
    "//javatests/com/google/android/apps/miphone/pcs/privateinference/library/oakutil:__subpackages__",
    "//javatests/com/google/android/apps/miphone/pcs/privateinference/service:__subpackages__",
    "//src/com/google/android/as/oss/privateinference/library/bsa:__subpackages__",
])

android_library(
    name = "blind_sign_auth_impl",
    srcs = ["BlindSignAuthImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/bsa/jni:blind_sign_auth_jni_bridge",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_client_timer_names",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "blind_sign_auth_impl_jvm",
    srcs = ["BlindSignAuthImpl.kt"],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth_jvm",
        "//src/com/google/android/as/oss/privateinference/library/bsa/jni:blind_sign_auth_jni_bridge_jvm",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_jvm",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_client_timer_names_jvm",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set_jvm",
        "//third_party/java/protobuf",
    ],
)

android_library(
    name = "phosphor_grpc_message_interface",
    srcs = ["PhosphorGrpcMessageInterface.kt"],
    deps = [
        "//privacy/net/common/proto:attest_and_sign_kt_proto_lite",
        "//privacy/net/common/proto:get_initial_data_kt_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config/impl:device_info",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/bsa/proto:arateaipblinding_kt_grpc_lite",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_client_timer_names",
        "//src/com/google/android/as/oss/privateinference/networkusage",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "phosphor_grpc_message_interface_jvm",
    srcs = ["PhosphorGrpcMessageInterface.kt"],
    visibility = [
        "//javatests/com/google/android/apps/miphone/pcs/privateinference/library/bsa:__subpackages__",
        "//src/com/google/android/as/oss/privateinference/library/bsa:__subpackages__",
    ],
    deps = [
        "//privacy/net/common/proto:attest_and_sign_kt_proto",
        "//privacy/net/common/proto:get_initial_data_kt_proto",
        "//src/com/google/android/as/oss/privateinference/config/impl:device_info_jvm",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth_jvm",
        "//src/com/google/android/as/oss/privateinference/library/bsa/proto:arateaipblinding_kt_grpc",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_client_timer_names_jvm",
        "//src/com/google/android/as/oss/privateinference/networkusage:networkusage_jvm",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set_jvm",
        "//third_party/java/flogger",
        "//third_party/java/grpc:core",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "android_keystore_attester",
    srcs = ["AndroidKeystoreAttester.kt"],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/attestation:android_keystore_attester_impl",
        "//src/com/google/android/as/oss/privateinference/library/bsa:blind_sign_auth",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:device_attestation_flag",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

# Make the BUILD file visible for open-source packaging.
exports_files(["BUILD"])
