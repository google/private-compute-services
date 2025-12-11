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
load("//third_party/protobuf/bazel:cc_proto_library.bzl", "cc_proto_library")
load("//third_party/protobuf/bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("//third_party/protobuf/bazel:java_proto_library.bzl", "java_proto_library")
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")

package(default_visibility = [
    "//visibility:public",
])

proto_library(
    name = "public_keys_proto",
    srcs = ["public_keys.proto"],
    deps = [
        "//third_party/protobuf:cpp_features_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

cc_proto_library(
    name = "public_keys_cc_proto",
    visibility = [
        "//visibility:public",
    ],
    deps = [":public_keys_proto"],
)

java_proto_library(
    name = "public_keys_java_proto",
    deps = [":public_keys_proto"],
)

java_lite_proto_library(
    name = "public_keys_java_proto_lite",
    deps = [":public_keys_proto"],
)

filegroup(
    name = "public_keys_prod_proto",
    srcs = ["res/raw/public_keys_prod_proto.binarypb"],
    visibility = [
        "//visibility:public",
    ],
)

android_library(
    name = "private_inference_service_stub_factory",
    srcs = ["PrivateInferenceServiceStubFactory.kt"],
    deps = [
        "//src/com/google/android/as/oss/feedback/gateway:http_client_helper",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//src/com/google/android/as/oss/privateinference/api:private_aratea_service_java_lite_grpc",
        "//src/com/google/android/as/oss/privateinference/config/impl:device_info",
        "//src/com/google/android/as/oss/privateinference/library:private_inference_request_metadata",
        "//src/com/google/android/as/oss/privateinference/transport:managed_channel_factory",
        "//third_party/java/grpc:okhttp_android",  # unuseddeps: keep buildcleaner: keep Runtime dependency for gRPC ManagedChannel
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "device_attestation_flag",
    srcs = ["DeviceAttestationFlag.kt"],
)

android_library(
    name = "private_inference_oak_async_client",
    srcs = [
        "AndroidKeystoreAttestationGenerator.kt",
        "PrivateInferenceOakAsyncClient.kt",
        "PrivateInferenceOakAsyncClientModule.java",
        "RequestLoggingHelpers.kt",
    ],
    manifest = "//src/com/google/android/as/oss/privateinference/library/oakutil:AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":device_attestation_flag",
        ":private_inference_client_timer_names",
        ":private_inference_service_stub_factory",
        ":public_keys_java_proto_lite",
        ":publisher_module",
        "//privacy/net/common/proto:spend_token_data_kt_proto_lite",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/api:anonymous_token_request_kt_proto_lite",
        "//src/com/google/android/as/oss/privateinference/api:device_attestation_request_kt_proto_lite",
        "//src/com/google/android/as/oss/privateinference/api:private_aratea_service_java_lite_grpc",
        "//src/com/google/android/as/oss/privateinference/api:private_aratea_service_kt_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config/impl:aratea_auth_flag",
        "//src/com/google/android/as/oss/privateinference/library:private_inference_request_metadata",
        "//src/com/google/android/as/oss/privateinference/library/attestation:android_keystore_attester_impl",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/logging",
        "//src/com/google/android/as/oss/privateinference/util/timers",
        "//src/com/google/android/as/oss/privateinference/util/timers:annotations",
        "//src/com/google/android/as/oss/privateinference/util/timers:pi_debug_log_timers",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set",
        "//third_party/kotlin/kotlinx_atomicfu:kotlinx_atomicfu-jvm",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "//third_party/oak/java/src/main/java/com/google/oak/client/grpc:stream_observer_session_client_no_jni_android",
        "//third_party/oak/java/src/main/java/com/google/oak/remote_attestation:attestation_verification_clock_android",
        "//third_party/oak/java/src/main/java/com/google/oak/session:session_config_builder_no_jni_android",
        "//third_party/oak/proto/session:session_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "attestation_publisher_flag",
    srcs = ["AttestationPublisherFlag.kt"],
)

android_library(
    name = "private_inference_file_attestation_publisher",
    srcs = ["PrivateInferenceFileAttestationPublisher.kt"],
    deps = [
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//third_party/java/protobuf/util:time_lite",
        "//third_party/oak/java/src/main/java/com/google/oak/session:attestation_publisher_android",
        "//third_party/oak/proto/attestation:verification_java_proto_lite",
        "//third_party/oak/proto/session:messages_kt_proto_lite",
        "//third_party/oak/proto/session:session_kt_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

android_library(
    name = "publisher_module",
    srcs = ["PublisherModule.kt"],
    deps = [
        ":attestation_publisher_flag",
        ":private_inference_file_attestation_publisher",
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//third_party/oak/java/src/main/java/com/google/oak/session:attestation_publisher_android",
        "//third_party/oak/proto/attestation:verification_java_proto_lite",
        "//third_party/oak/proto/session:messages_kt_proto_lite",
        "//third_party/oak/proto/session:session_kt_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "private_inference_verification_keys_prod_module",
    srcs = [
        "PrivateInferenceVerificationKeysProdModule.java",
    ],
    manifest = "//src/com/google/android/as/oss/privateinference/library/oakutil:AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        ":public_keys_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "private_inference_client_timer_names",
    srcs = ["PrivateInferenceClientTimerNames.kt"],
)

kt_jvm_library_with_nullness_check(
    name = "private_inference_client_timer_names_jvm",
    srcs = ["PrivateInferenceClientTimerNames.kt"],
)

android_library(
    name = "system_time_module",
    srcs = ["SystemTimeModule.kt"],
    deps = [
        "//src/com/google/android/as/oss/common/time",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "unattested_client_session_config_builder_module",
    srcs = [
        "UnattestedClientSessionConfigBuilderModule.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/oakutil/jni:pi_client_session_config_jni",
        "//third_party/oak/java/src/main/java/com/google/oak/session:session_config_builder_android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "peer_attested_client_session_config_builder_module",
    srcs = [
        "PeerAttestedClientSessionConfigBuilderModule.kt",
    ],
    deps = [
        ":attestation_verification_exception",
        ":peer_attested_client_session_config_builder",
        ":public_keys_java_proto_lite",
        "//java/com/google/protobuf/util/kotlin:javatime_lite-ktx",
        "//third_party/oak/java/src/main/java/com/google/oak/remote_attestation:attestation_verification_clock_android",
        "//third_party/oak/java/src/main/java/com/google/oak/session:attestation_publisher_android",
        "//third_party/oak/java/src/main/java/com/google/oak/session:session_config_builder_no_jni_android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "peer_attested_client_session_config_builder",
    srcs = [
        "PeerAttestedClientSessionConfigBuilder.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/oakutil/jni:pi_client_session_config_jni",
        "//third_party/oak/java/src/main/java/com/google/oak/remote_attestation:attestation_verification_clock_android",
        "//third_party/oak/java/src/main/java/com/google/oak/session:attestation_publisher_android",
        "//third_party/oak/java/src/main/java/com/google/oak/session:session_config_builder_no_jni_android",
    ],
)

android_library(
    name = "attestation_verification_exception",
    srcs = [
        "AttestationVerificationException.kt",
    ],
)
