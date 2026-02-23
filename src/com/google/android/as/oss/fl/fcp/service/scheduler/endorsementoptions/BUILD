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
    name = "internal",
    srcs = ["InternalModule.java"],
    visibility = [
        "//visibility:public",
    ],
    deps = [
        ":endorsement_client_type",
        ":endorsementoptions",
        ":pcs_internal_endorsement_options",
        "//third_party/fcp/protos/confidentialcompute:access_policy_endorsement_options_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "release",
    srcs = ["ReleaseModule.java"],
    deps = [
        ":endorsement_client_type",
        ":endorsementoptions",
        ":pcs_release_endorsement_options",
        "//third_party/fcp/protos/confidentialcompute:access_policy_endorsement_options_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "noop",
    srcs = [
        "NoopEndorsementOptionsProviderImpl.java",
        "NoopModule.java",
    ],
    deps = [
        ":endorsement_client_type",
        ":endorsementoptions",
        "//third_party/fcp/protos/confidentialcompute:access_policy_endorsement_options_java_proto_lite",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "endorsementoptions",
    srcs = [
        "EndorsementOptionsProvider.java",
        "EndorsementOptionsProviderImpl.java",
    ],
    deps = [
        ":endorsement_client_type",
        "//java/com/google/protobuf/contrib/android:proto_parsers",
        "//third_party/fcp/protos/confidentialcompute:access_policy_endorsement_options_java_proto_lite",
    ],
)

android_library(
    name = "endorsement_client_type",
    srcs = ["EndorsementClientType.java"],
)
