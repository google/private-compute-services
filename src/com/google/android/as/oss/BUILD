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

# Top level build file for building PCS in OSS.

load("@bazel_rules_android//android:rules.bzl", "android_binary")

android_binary(
    name = "pcs",
    srcs = ["PrivateComputeServicesApplication.java"],
    manifest = "AndroidManifest.xml",
    multidex = "native",
    resource_files = glob(["res/**"]),
    deps = [
        # Common and Core Modules
        "//src/com/google/android/as/oss/common/config/impl:flags_impl",
        "//src/com/google/android/as/oss/common/flavor:release",
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/common/time:system_module",
        "//src/com/google/android/as/oss/common/jobs:heartbeat_module",
        "//src/com/google/android/as/oss/common/broadcastreceivers:boot_completed_broadcast_receiver",

        # AI and Attestation
        "//src/com/google/android/as/oss/ai/config/impl",
        "//src/com/google/android/as/oss/ai/service",
        "//src/com/google/android/as/oss/attestation/config/impl",
        "//src/com/google/android/as/oss/attestation/impl:impl_module",
        "//src/com/google/android/as/oss/attestation/jobs",

        # Data Attribution
        "//src/com/google/android/as/oss/dataattribution:prod_modules",

        # Delegated UI
        "//src/com/google/android/as/oss/delegatedui:prod_modules",

        # Feedback
        "//src/com/google/android/as/oss/feedback:prod_modules",

        # Federated Compute
        "//src/com/google/android/as/oss/fl:federated_module",
        "//src/com/google/android/as/oss/fl/fcp/service",
        "//src/com/google/android/as/oss/fl/federatedcompute/attestation:client_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/config",
        "//src/com/google/android/as/oss/fl/federatedcompute/config/impl:config_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/init",
        "//src/com/google/android/as/oss/fl/federatedcompute/logging",
        "//src/com/google/android/as/oss/fl/federatedcompute/logging/statsd:module",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/module:config_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/module:connector_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/scheduler:scheduler_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/training:training_module",
        "//src/com/google/android/as/oss/fl/server:service",
        "@federated_compute//fcp/client:fl_runner",

        # GRPC and HTTP
        "//src/com/google/android/as/oss/grpc:grpc_pcs",
        "//src/com/google/android/as/oss/grpc/impl:client",
        "//src/com/google/android/as/oss/grpc/impl:server",
        "//src/com/google/android/as/oss/grpc/perapp:pcs",
        "//src/com/google/android/as/oss/http/config/impl",
        "//src/com/google/android/as/oss/http/service",

        # Logging and Network Usage
        "//src/com/google/android/as/oss/logging/impl:impl_module",
        "//src/com/google/android/as/oss/networkusage/db/impl",
        "//src/com/google/android/as/oss/networkusage/ui/content/impl:public_module",
        "//src/com/google/android/as/oss/networkusage/ui/user",

        # Protected Download
        "//src/com/google/android/as/oss/pd/attestation/impl",
        "//src/com/google/android/as/oss/pd/channel/impl",
        "//src/com/google/android/as/oss/pd/common:common_module",
        "//src/com/google/android/as/oss/pd/config/impl",
        "//src/com/google/android/as/oss/pd/keys/impl:android_keystore_module",
        "//src/com/google/android/as/oss/pd/keys/impl:encryption_module",
        "//src/com/google/android/as/oss/pd/networkusage/impl",
        "//src/com/google/android/as/oss/pd/persistence/preferencesimpl",
        "//src/com/google/android/as/oss/pd/processor/impl:module",
        "//src/com/google/android/as/oss/pd/service",
        "//src/com/google/android/as/oss/pd/virtualmachine/impl:module",

        # PIR
        "//src/com/google/android/as/oss/pir/service",

        # Policies
        "//src/com/google/android/as/oss/policies/impl:prod_policies",

        # Settings and Survey
        "//src/com/google/android/as/oss/settings/ui",
        "//src/com/google/android/as/oss/survey/config/impl",
        "//src/com/google/android/as/oss/survey/service",

        # Third-Party Libraries
        "//third_party/java/hilt:hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)
