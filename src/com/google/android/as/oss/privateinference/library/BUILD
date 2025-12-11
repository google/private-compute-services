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
    name = "private_inference_request_metadata",
    srcs = [
        "PrivateInferenceRequestMetadata.java",
    ],
)

android_library(
    name = "oak_server_response_observer",
    srcs = [
        "BaseOakServerStreamResponseObserver.java",
    ],
    deps = [
        "//src/com/google/android/as/oss/privateinference/library/oakutil:attestation_verification_exception",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_java_proto_lite",
        "//third_party/oak/java/src/main/java/com/google/oak/client/grpc:stream_observer_session_client_no_jni_android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_stub",
    ],
)

android_library(
    name = "oak_server_request_observer",
    srcs = [
        "BaseOakServerStreamRequestReader.java",
    ],
    deps = [
        ":private_inference_request_metadata",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_oak_async_client",
        "//src/com/google/android/as/oss/privateinference/service/api:private_inference_java_proto_lite",
        "//third_party/oak/java/src/main/java/com/google/oak/client/grpc:stream_observer_session_client_no_jni_android",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_stub",
    ],
)
