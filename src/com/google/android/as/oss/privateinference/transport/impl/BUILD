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
load("//third_party/protobuf/bazel:proto_library.bzl", "proto_library")
load("//third_party/protobuf/build_defs:kt_jvm_proto_library.bzl", "kt_jvm_lite_proto_library")

package(default_visibility = [
    "//java/com/google/android/apps/internal/mdi/privatearatea:__pkg__",
    "//java/com/google/android/libraries/mdi/privatearatea:__pkg__",
    "//javatests/com/google/android/libraries/mdi/privatearatea:__pkg__",
    "//visibility:public",
])

android_library(
    name = "pi_factory",
    srcs = ["PrivateInferenceManagedChannelFactory.kt"],
    deps = [
        "//privacy/net/common/proto:spend_token_data_kt_proto_lite",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//src/com/google/android/as/oss/privateinference/config/impl:proxy_auth_flag",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token",
        "//src/com/google/android/as/oss/privateinference/library/bsa/token:token_provider",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_client_timer_names",
        "//src/com/google/android/as/oss/privateinference/logging",
        "//src/com/google/android/as/oss/privateinference/transport",
        "//src/com/google/android/as/oss/privateinference/transport:constants",
        "//src/com/google/android/as/oss/privateinference/transport:managed_channel_factory",
        "//src/com/google/android/as/oss/privateinference/transport:proxy_config_manager",
        "//src/com/google/android/as/oss/privateinference/util/timers:annotations",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set",
        "//third_party/java/android_libs/cronet",
        "//third_party/java/android_libs/cronet:httpengine_native_provider_implementation",
        "//third_party/java/grpc:cronet_api_android",
        "//third_party/java/grpc:okhttp_android",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "transport_module",
    srcs = [
        "ProxyConfigModule.kt",
        "TransportModule.kt",
    ],
    deps = [
        ":pi_factory",
        ":proxy_config_refresh_worker",
        ":timestamped_proxy_configs_kt_proto_lite",
        "//java/com/google/android/apps/common/inject/annotation",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/transport:managed_channel_factory",
        "//src/com/google/android/as/oss/privateinference/transport:proxy_config_manager",
        "//third_party/java/android_libs/cronet",
        "//third_party/java/androidx/datastore",
        "//third_party/java/androidx/datastore/core",
        "//third_party/java/androidx/datastore/core:proto_serializer-android",
        "//third_party/java/androidx/work",
        "//third_party/java/grpc:cronet_api_android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "proxy_config_manager_impl",
    srcs = [
        "ProxyConfigManagerImpl.kt",
    ],
    deps = [
        ":proxy_config_refresh_worker",
        ":timestamped_proxy_configs_kt_proto_lite",
        "//google/internal/ppn/phosphor/v1:arateaipblinding_kt_grpc_lite",
        "//java/com/google/protobuf/util/kotlin:javatime_lite-ktx",
        "//privacy/net/common/proto:get_proxy_config_kt_proto_lite",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/time",
        "//src/com/google/android/as/oss/logging:enums_java_proto_lite",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:private_inference_client_timer_names",
        "//src/com/google/android/as/oss/privateinference/logging",
        "//src/com/google/android/as/oss/privateinference/networkusage",
        "//src/com/google/android/as/oss/privateinference/transport",
        "//src/com/google/android/as/oss/privateinference/transport:proxy_config_manager",
        "//src/com/google/android/as/oss/privateinference/util/timers:annotations",
        "//src/com/google/android/as/oss/privateinference/util/timers:timer_set",
        "//third_party/java/android_libs/guava_jdk5:net",
        "//third_party/java/androidx/datastore/core",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-android",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)

proto_library(
    name = "timestamped_proxy_configs",
    srcs = ["timestamped_proxy_configs.proto"],
    deps = [
        "@com_google_protobuf//:timestamp_proto",
    ],
)

kt_jvm_lite_proto_library(
    name = "timestamped_proxy_configs_kt_proto_lite",
    deps = [":timestamped_proxy_configs"],
)

android_library(
    name = "proxy_config_refresh_worker",
    srcs = [
        "ProxyConfigControlPlane.kt",
        "ProxyConfigRefreshWorker.kt",
    ],
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        ":timestamped_proxy_configs_kt_proto_lite",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/privateinference/config",
        "//third_party/java/androidx/datastore/core",
        "//third_party/java/androidx/work",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_flogger_google_extensions",
    ],
)
