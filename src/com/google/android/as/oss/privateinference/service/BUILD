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
    name = "service",
    srcs = glob([
        "*.java",
    ]),
    deps = [
        ":logging_metric_id_provider",
        ":logging_metric_id_release_provider_module",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/flavor",
        "//src/com/google/android/as/oss/grpc:annotations",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library:oak_server_request_observer",
        "//src/com/google/android/as/oss/privateinference/library:oak_server_response_observer",
        "//src/com/google/android/as/oss/privateinference/library:private_inference_request_metadata",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:attestation_verification_exception",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:peer_attested_client_session_config_builder_module",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_oak_async_client",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_verification_keys_prod_module",
        "//src/com/google/android/as/oss/privateinference/logging",
        "//src/com/google/android/as/oss/privateinference/networkusage",
        "//src/com/google/android/as/oss/privateinference/service/api:metadata_parcel_file_descriptor_keys",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_grpc",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/util/timers",
        "//src/com/google/android/as/oss/privateinference/util/timers:annotations",
        "//src/com/google/android/as/oss/privateinference/util/timers:latency_logging_timers",
        "//src/com/google/android/as/oss/privateinference/util/timers:pi_debug_log_timers",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set",
        "//src/com/google/android/as/oss/privateinference/util/timers:trace_timers",
        "//third_party/oak/java/src/main/java/com/google/oak/client/grpc:stream_observer_session_client_no_jni_android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:io_grpc_grpc_context",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "service_internal",
    srcs = glob([
        "*.java",
    ]),
    deps = [
        ":logging_metric_id_internal_provider_module",
        ":logging_metric_id_provider",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/flavor",
        "//src/com/google/android/as/oss/grpc:annotations",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library:oak_server_request_observer",
        "//src/com/google/android/as/oss/privateinference/library:oak_server_response_observer",
        "//src/com/google/android/as/oss/privateinference/library:private_inference_request_metadata",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:attestation_verification_exception",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:peer_attested_client_session_config_builder_module",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_oak_async_client",
        "//src/com/google/android/as/oss/privateinference/logging",
        "//src/com/google/android/as/oss/privateinference/networkusage",
        "//src/com/google/android/as/oss/privateinference/service/api:metadata_parcel_file_descriptor_keys",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_grpc",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/util/timers",
        "//src/com/google/android/as/oss/privateinference/util/timers:annotations",
        "//src/com/google/android/as/oss/privateinference/util/timers:latency_logging_timers",
        "//src/com/google/android/as/oss/privateinference/util/timers:pi_debug_log_timers",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set",
        "//src/com/google/android/as/oss/privateinference/util/timers:trace_timers",
        "//third_party/oak/java/src/main/java/com/google/oak/client/grpc:stream_observer_session_client_no_jni_android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_binder",
        "@maven//:io_grpc_grpc_context",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "logging_metric_id_qualifiers",
    srcs = [
        "LoggingMetricIdQualifiers.kt",
    ],
    deps = [
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "logging_metric_id_provider",
    srcs = [
        "LoggingMetricIdProvider.kt",
    ],
    deps = [
        ":logging_metric_id_qualifiers",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_java_proto_lite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "logging_metric_id_release_provider_module",
    srcs = [
        "LoggingMetricIdMapReleaseModule.kt",
    ],
    deps = [
        ":logging_metric_id_qualifiers",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "logging_metric_id_internal_provider_module",
    srcs = [
        "LoggingMetricIdMapInternalModule.kt",
    ],
    deps = [
        ":logging_metric_id_qualifiers",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "private_inference_grpc_timer_names",
    srcs = ["PrivateInferenceGrpcTimerNames.java"],
    deps = [],
)
