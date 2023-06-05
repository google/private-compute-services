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

android_library(
    name = "impl",
    srcs = [
        "ContentMapEntryBuilder.java",
        "NetworkUsageLogContentMapImpl.java",
    ],
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/networkusage/api:connection_key_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//src/com/google/android/as/oss/networkusage/ui/user:res_lib",
        "//third_party/java/auto:auto_value",
        "//third_party/java/hilt:hilt-android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "public_module",
    srcs = [
        "NetworkUsageLogContentModule.java",
    ],
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        ":impl",
        "//src/com/google/android/as/oss/attestation",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//src/com/google/android/as/oss/networkusage/ui/user:res_lib",
        "//src/com/google/android/as/oss/protos:pcs_feature_enum_java_proto_lite",
        "//third_party/java/hilt:hilt-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
    ],
)
