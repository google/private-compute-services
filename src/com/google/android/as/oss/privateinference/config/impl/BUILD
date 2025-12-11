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

package(default_visibility = [
    "//java/com/google/android/apps/internal/mdi/privatearatea:__subpackages__",
    "//java/com/google/android/libraries/mdi/privatearatea:__subpackages__",
    "//visibility:public",
])

android_library(
    name = "impl",
    srcs = [
        "PcsConfigArateaAuthFlag.kt",
        "PcsConfigAttestationFlag.kt",
        "PcsConfigAttestationPublisherFlag.kt",
        "PcsConfigProxyAuthFlag.kt",
        "PcsConfigTransportFlag.kt",
        "PrivateInferenceInternalConfigModule.java",
        "PrivateInferenceInternalConfigReader.java",
    ],
    deps = [
        ":aratea_auth_flag",
        ":device_info_module",  # buildcleaner: keep
        ":proxy_auth_flag",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:attestation_publisher_flag",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:device_attestation_flag",
        "//src/com/google/android/as/oss/privateinference/transport",
        "@maven//:androidx_core_core",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "proxy_auth_flag",
    srcs = ["ProxyAuthFlag.kt"],
)

android_library(
    name = "aratea_auth_flag",
    srcs = ["ArateaAuthFlag.kt"],
)

android_library(
    name = "device_info",
    srcs = ["DeviceInfo.kt"],
    deps = [
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_stub",
    ],
)

kt_jvm_library_with_nullness_check(
    name = "device_info_jvm",
    srcs = ["DeviceInfo.kt"],
    deps = [
        "//third_party/java/flogger",
        "//third_party/java/grpc:core",
        "//third_party/java/grpc:stub",
    ],
)

android_library(
    name = "device_info_module",
    srcs = ["DeviceInfoModule.kt"],
    deps = [
        ":device_info",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "noop_device_info_module",
    srcs = ["NoopDeviceInfoModule.kt"],
    deps = [
        ":device_info",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
    ],
)

android_library(
    name = "release_module",
    srcs = [
        "PcsConfigArateaAuthFlag.kt",
        "PcsConfigAttestationFlag.kt",
        "PcsConfigAttestationPublisherFlag.kt",
        "PcsConfigProxyAuthFlag.kt",
        "PcsConfigTransportFlag.kt",
        "PrivateInferenceConfigModule.java",
        "PrivateInferenceConfigReader.java",
    ],
    deps = [
        ":noop_device_info_module",  # buildcleaner: keep
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/privateinference:annotations",
        "//src/com/google/android/as/oss/privateinference/config",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:attestation_publisher_flag",
        "//src/com/google/android/as/oss/privateinference/library/oakutil:device_attestation_flag",
        "//src/com/google/android/as/oss/privateinference/transport",
        "@maven//:androidx_core_core",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)
