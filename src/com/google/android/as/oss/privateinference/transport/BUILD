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

load("@bazel_rules_android//android:rules.bzl", "android_library", "kt_jvm_library_with_nullness_check")
load("//java/com/google/net/cronet/providers_checker:providers_checker.bzl", "PROVIDERS", "check_bundled_cronet_providers")

package(default_visibility = [
    "//visibility:public",
])

android_library(
    name = "transport",
    srcs = ["TransportFlag.kt"],
    deps = [],
)

android_library(
    name = "proxy_config_manager",
    srcs = ["ProxyConfigManager.kt"],
    deps = [
        ":transport",
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "managed_channel_factory",
    srcs = ["ManagedChannelFactory.kt"],
    deps = [
        "@maven//:io_grpc_grpc_api",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "managed_channel_factory_jvm",
    srcs = ["ManagedChannelFactory.kt"],
    deps = [
        "@maven//:io_grpc_grpc_api",
    ],
)

android_library(
    name = "constants",
    srcs = [
        "TransportConstants.kt",
    ],
    deps = [],
)

android_library(
    name = "phosphor_factory",
    srcs = ["PhosphorManagedChannelFactory.kt"],
    deps = [
        ":constants",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//third_party/java/android_libs/cronet:httpengine_native_provider_implementation",
        "//third_party/java/grpc:cronet_api_android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

check_bundled_cronet_providers(
    name = "cronet_providers_checker_release",
    binary_target = "//src/com/google/android/as/oss:pcs-release",
    expected_providers = [
        PROVIDERS.HTTPENGINE,
        PROVIDERS.STATICALLY_LINKED_CRONET,
        PROVIDERS.FALLBACK_CRONET,
    ],
)

android_library(
    name = "proxy_config_provider_type",
    srcs = ["ProxyConfigProviderType.kt"],
)
