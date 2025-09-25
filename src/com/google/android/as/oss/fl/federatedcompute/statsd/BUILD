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

android_library(
    name = "statsd",
    srcs = glob(["*.java"]),
    deps = [
        "//java/com/google/common/android/base:ticker",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/fl/fc/api:empty_example_iterator",
        "//src/com/google/android/as/oss/fl/fc/api:training_java_proto_lite",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/examplegenerator:example_generator",
        "//src/com/google/android/as/oss/logging:api",
        "//src/com/google/android/as/oss/logging:atoms_java_proto_lite",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/protos:pcs_query_java_proto_lite",
        "//src/com/google/android/as/oss/protos:pcs_statsquery_java_proto_lite",
        "//third_party/fcp/client:selector_context_java_proto_lite",
        "//third_party/java/android_libs/guava_jdk5:primitives",
        "//third_party/java/proto:any_java_proto_lite",
        "@federated_compute//fcp/client:fl_runner",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
        "@maven//:javax_inject_javax_inject",
    ],
)
