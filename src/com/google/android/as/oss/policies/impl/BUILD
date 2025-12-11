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
    name = "asset_loader",
    srcs = [
        "AssetLoader.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/proto:manifest_java_proto_lite",
        "//src/com/google/android/as/oss/policies/api:policy_map",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_guava_guava",
        "@private_compute_libraries//java/com/google/android/libraries/pcc/chronicle/api/policy/proto:policy_java_proto_lite",
    ],
)

android_library(
    name = "no_policies",
    srcs = [
        "NoPoliciesModule.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/policies/api:policy_map",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "prod_policies",
    srcs = [
        "ProdPoliciesModule.kt",
    ],
    assets = [
        "//src/com/google/android/as/oss/assets/federatedcompute:AmbientContextPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:AppLaunchPredictionMetricsPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:AutofillPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:ContentCapturePerformanceDataPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:GPPServicePolicy_FederatedCompute_GPPS_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:GPPServicePolicyV2_FederatedCompute_GPPS_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:LiveTranslatePolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:NotificationIntelligencePolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:NowPlayingUsagePolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:PecanContextPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:PecanConversationFragmentEventPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:PecanConversationThreadEventPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:PecanLatencyAnalyticsEventPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:PecanMessageEventPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:PecanUsageEventPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:PwmPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:SafecommsPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:SearchPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:SmartSelectAnalyticsPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:SmartSelectLearningPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:ToastQueryPolicy_FederatedCompute_ASI_PROD",
        "//src/com/google/android/as/oss/assets/federatedcompute:PlatformLoggingPolicy_FederatedCompute_PCS_RELEASE",
    ],
    assets_dir = "",
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        ":asset_loader",
        "//src/com/google/android/as/oss/policies/api:policy_map",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
        "@private_compute_libraries//java/com/google/android/libraries/pcc/chronicle/api/policy/proto:policy_java_proto_lite",
    ],
)
