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
        "PccAttestationMeasurementClientImpl.java",
    ],
    deps = [
        "//google/internal/android/keyattestation/v1:key_attestation_service_java_proto_lite",
        "//google/internal/android/keyattestation/v1:key_attestation_service_lite_grpc",
        "//java/com/google/common/time:time-android",
        "//java/com/google/protobuf/util:javatime_lite",
        "//src/com/google/android/as/oss/attestation",
        "//src/com/google/android/as/oss/attestation/api:attestation_java_proto_lite",
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_api",
    ],
)

android_library(
    name = "impl_module",
    srcs = [
        "PccAttestationMeasurementClientModule.java",
    ],
    deps = [
        ":impl",
        "//src/com/google/android/as/oss/attestation",
        "//src/com/google/android/as/oss/attestation/config",
        "//src/com/google/android/as/oss/common",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//third_party/java/grpc:okhttp_android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
    ],
)
