# Copyright 2023 Google LLC
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

package(default_visibility = ["//visibility:public"])

exports_files([
    "ConnectionDetails.java",
    "NetworkUsageEntity.java",
])

android_library(
    name = "db",
    srcs = glob(
        ["*.java"],
        exclude = ["NetworkUsageLogRepository.java"],
    ),
    plugins = [
        "//third_party/java/auto:auto_value_parcel",
    ],
    deps = [
        "//src/com/google/android/as/oss/attestation",
        "//src/com/google/android/as/oss/networkusage/api:connection_key_java_proto_lite",
        "//third_party/java/auto:auto_value",
        "//third_party/java/auto:auto_value_parcel_adapter",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_lifecycle_lifecycle_livedata",
        "@maven//:androidx_lifecycle_lifecycle_livedata_core",
        "@maven//:androidx_room_room_common",
        "@maven//:androidx_room_room_runtime",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_lite",
        "@maven//:javax_inject_javax_inject",
        "@private_compute_libraries//java/com/google/android/libraries/pcc/chronicle/api/policy/proto:policy_java_proto_lite",
    ],
)

android_library(
    name = "repository",
    srcs = ["NetworkUsageLogRepository.java"],
    deps = [
        ":db",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "@maven//:androidx_lifecycle_lifecycle_livedata_core",
        "@maven//:com_google_guava_guava",
    ],
)
