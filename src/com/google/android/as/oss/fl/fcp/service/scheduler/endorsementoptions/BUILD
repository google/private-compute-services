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
load("//intelligence/fc/build:fc.bzl", "fc_endorsement_options")

package(default_visibility = ["//visibility:public"])

####################################################################################################
# Endorsement options for PCS.
#
# To add new endorsement options for PCS:
# 1. Follow instructions at [redacted] to create new keys (saving the final step
# for after 5 below).
# 2. Create release and internal `fc_endorsement_options` rules below.
# 3. Create a new `EndorsementClientType` enum value for your new keys.
# 4. Add the new to the respective modules (ReleaseModule.java and InternalModule.java) pointing to
#    the `fc_endorsement_options` you defined in 2.
# 5. Include the new keys in the public GithHub declaration of verifying keys (see @timonvo's
#    comment http://b/410519159#comment16).
# 6. Complete the final step of [redacted] once you're absolutely sure you've
#    checked in both keys .
####################################################################################################

fc_endorsement_options(
    name = "pcs_release_endorsement_options",
    android_manifest = "//src/com/google/android/as/oss/fl/fc/service:AndroidManifest.xml",
    key = "//intelligence/fc/config/public_keys:78712.access-policy-endorsement-verifying-key.binpb",
)

fc_endorsement_options(
    name = "pcs_internal_endorsement_options",
    android_manifest = "//src/com/google/android/as/oss/fl/fc/service:AndroidManifest.xml",
    key = "//intelligence/fc/config/public_keys:78712.googler-only-access-policy-endorsement-verifying-key.binpb",
)

android_library(
    name = "internal",
    srcs = ["InternalModule.java"],
    visibility = [
        "//intelligence/fc:users",
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
