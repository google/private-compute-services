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
    name = "config",
    srcs = [
        "DelegatedUiConfig.kt",
        "DelegatedUiConfigConstants.kt",
        "DelegatedUiConfigReader.kt",
        "DelegatedUiFlags.kt",
    ],
    deps = [
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/security/api:package_security_info_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/config:data_service_config_kt_proto_lite",
        "//third_party/java/android/android_sdk_linux/extras/android/compatibility/v4",
    ],
)

android_library(
    name = "module",
    srcs = [
        "DelegatedUiConfigModule.kt",
    ],
    deps = [
        ":config",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)
