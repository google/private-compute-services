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
        "BlobProtoUtils.java",
        "ProtectedDownloadProcessorImpl.java",
        "SanityChecks.java",
    ],
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//src/com/google/android/as/oss/pd/api:protected_download_java_proto_lite",
        "//src/com/google/android/as/oss/pd/attestation",
        "//src/com/google/android/as/oss/pd/channel",
        "//src/com/google/android/as/oss/pd/common",
        "//src/com/google/android/as/oss/pd/config",
        "//src/com/google/android/as/oss/pd/keys",
        "//src/com/google/android/as/oss/pd/networkusage",
        "//src/com/google/android/as/oss/pd/persistence",
        "//src/com/google/android/as/oss/pd/persistence:client_persistent_state_java_proto_lite",
        "//src/com/google/android/as/oss/pd/processor",
        "//src/com/google/android/as/oss/pd/service/api:download_service_java_grpc_lite",
        "//src/com/google/android/as/oss/pd/service/api:download_service_java_proto_lite",
        "//src/com/google/android/as/oss/pd/service/api:programblobs_java_grpc_lite",
        "//src/com/google/android/as/oss/pd/service/api:programblobs_java_proto_lite",
        "//third_party/java/auto:auto_value",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_stub",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "module",
    srcs = [
        "BlobProtoUtilsModule.java",
        "ProtectedDownloadProcessorModule.java",
    ],
    deps = [
        ":impl",
        "//src/com/google/android/as/oss/pd/common",
        "//src/com/google/android/as/oss/pd/config",
        "//src/com/google/android/as/oss/pd/processor",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)
