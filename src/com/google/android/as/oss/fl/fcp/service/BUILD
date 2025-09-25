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

package(default_visibility = ["//visibility:public"])

ARCS_POLICY = [
    "//java/com/google/android/libraries/pcc/chronicle/api/policy",
    "@private_compute_libraries//java/com/google/android/libraries/pcc/chronicle/api/policy/proto:policy_java_proto_lite",
]

exports_files(
    ["AndroidManifest.xml"],
    visibility = ["//src/com/google/android/as/oss/fl/fc/service/scheduler:__subpackages__"],
)

android_library(
    name = "service",
    srcs = glob(["*.java"]),
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    deps = ARCS_POLICY + [
        "//src/com/google/android/as/oss/common",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/base",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/consent:opted_in",
        "//src/com/google/android/as/oss/common/consent/config",
        "//src/com/google/android/as/oss/common/flavor",
        "//src/com/google/android/as/oss/fl:annotations",
        "//src/com/google/android/as/oss/fl/fc/api",
        "//src/com/google/android/as/oss/fl/fc/api:training_java_proto_lite",
        "//src/com/google/android/as/oss/fl/fc/service/util",
        "//src/com/google/android/as/oss/fl/federatedcompute/config",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/config",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/module:config_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/util",
        "//src/com/google/android/as/oss/fl/localcompute:file_copy_start_query",
        "//src/com/google/android/as/oss/fl/localcompute:optional_module",
        "//src/com/google/android/as/oss/fl/localcompute:resource_manager",
        "//src/com/google/android/as/oss/fl/localcompute:utils",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/api:connection_key_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//src/com/google/android/as/oss/protos:pcs_query_java_proto_lite",
        "//third_party/fcp/client:selector_context_java_proto_lite",
        "//third_party/java/proto:any_java_proto_lite",
        "@federated_compute//fcp/client:fl_runner",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_core_core",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_lite",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_checkerframework_checker_qual",
    ],
)
