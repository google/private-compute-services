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

load("//src/com/google/android/as/oss:build_defs.bzl", "pcs_dev_and_mpm")
load("//testing/build_defs:text_proto_test.bzl", "text_proto_test")
load("//tools/build_defs/testing:bzl_library.bzl", "bzl_library")

package(default_visibility = [":pcs_visibility"])

pcs_dev_and_mpm(
    name = "release",
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "//src/com/google/android/as/oss/ai/config/impl",  # buildcleaner: keep
        "//src/com/google/android/as/oss/ai/service",  # buildcleaner: keep
        "//src/com/google/android/as/oss/attestation/config/impl",  # buildcleaner: keep
        "//src/com/google/android/as/oss/attestation/impl:impl_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/attestation/jobs",  # buildcleaner: keep
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/broadcastreceivers:boot_completed_broadcast_receiver",  # buildcleaner: keep
        "//src/com/google/android/as/oss/common/config/impl:flags_impl",
        "//src/com/google/android/as/oss/common/config/impl:pcs_common_config_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/common/flavor:release",  # buildcleaner: keep # setting build flavor to release
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/common/jobs:heartbeat_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/common/jobs:worker_factories_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/common/statslog:internal_stats_log_config_executor_module",
        "//src/com/google/android/as/oss/common/time:system_module",
        "//src/com/google/android/as/oss/conversationid",
        "//src/com/google/android/as/oss/dataattribution:prod_modules",  # buildcleaner: keep
        "//src/com/google/android/as/oss/delegatedui:prod_modules",  # buildcleaner: keep
        "//src/com/google/android/as/oss/feedback:prod_modules",  # buildcleaner: keep
        "//src/com/google/android/as/oss/feedback/gateway/service",  # buildcleaner: keep
        "//src/com/google/android/as/oss/fl:federated_module",
        "//src/com/google/android/as/oss/fl/fc/service",
        "//src/com/google/android/as/oss/fl/fc/service/scheduler/endorsementoptions:release",  # buildcleaner: keep # setting endorsement key to release
        "//src/com/google/android/as/oss/fl/federatedcompute/attestation:client_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/fl/federatedcompute/config",
        "//src/com/google/android/as/oss/fl/federatedcompute/config/impl:config_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/fl/federatedcompute/init",
        "//src/com/google/android/as/oss/fl/federatedcompute/logging",
        "//src/com/google/android/as/oss/fl/federatedcompute/logging/statsd:module",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/module:config_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/module:connector_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/statsd/scheduler:scheduler_module",
        "//src/com/google/android/as/oss/fl/federatedcompute/training:training_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/fl/server:service",  # buildcleaner: keep
        "//src/com/google/android/as/oss/grpc:grpc_pcs",
        "//src/com/google/android/as/oss/grpc/impl:client",  # buildcleaner: keep
        "//src/com/google/android/as/oss/grpc/impl:server",  # buildcleaner: keep
        "//src/com/google/android/as/oss/grpc/perapp:pcs",  # buildcleaner: keep
        "//src/com/google/android/as/oss/http/config/impl",  # buildcleaner: keep
        "//src/com/google/android/as/oss/http/service",  # buildcleaner: keep
        "//src/com/google/android/as/oss/logging/impl:impl_module",
        "//src/com/google/android/as/oss/networkusage/db/impl",
        "//src/com/google/android/as/oss/networkusage/ui/content/impl:public_module",
        "//src/com/google/android/as/oss/networkusage/ui/user",
        "//src/com/google/android/as/oss/nowplaying:service",  # buildcleaner: keep
        "//src/com/google/android/as/oss/nowplaying/config/impl",  # buildcleaner: keep
        "//src/com/google/android/as/oss/pd/attestation/impl",  # buildcleaner: keep
        "//src/com/google/android/as/oss/pd/channel/impl",
        "//src/com/google/android/as/oss/pd/common:common_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/pd/config/impl",
        "//src/com/google/android/as/oss/pd/keys/impl:android_keystore_module",
        "//src/com/google/android/as/oss/pd/keys/impl:encryption_module",
        "//src/com/google/android/as/oss/pd/networkusage/impl",
        "//src/com/google/android/as/oss/pd/persistence/preferencesimpl",
        "//src/com/google/android/as/oss/pd/processor/impl:module",
        "//src/com/google/android/as/oss/pd/service",
        "//src/com/google/android/as/oss/pd/virtualmachine/impl:module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/pir/service",  # buildcleaner: keep
        "//src/com/google/android/as/oss/policies/impl:prod_policies",
        "//src/com/google/android/as/oss/privateinference/config/impl:release_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/privateinference/library/bsa:bsa_modules",  # buildcleaner: keep
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_verification_keys_prod_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/privateinference/logging:logging_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/privateinference/networkusage:module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/privateinference/service",  # buildcleaner: keep
        "//src/com/google/android/as/oss/privateinference/transport/impl:transport_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/settings/ui",
        "//src/com/google/android/as/oss/survey/config/impl",  # buildcleaner: keep
        "//src/com/google/android/as/oss/survey/service",  # buildcleaner: keep
        "//third_party/java/androidx/work",
        "//third_party/java/dagger/hilt:hilt_android_app",
        "@federated_compute//fcp/client:fl_runner",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)
