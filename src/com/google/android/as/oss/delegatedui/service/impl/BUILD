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
    name = "impl",
    srcs = [
        "ActiveSessionRequest.kt",
        "DelegatedUiServiceImpl.kt",
        "UnsupportedDelegatedUiServiceImpl.kt",
    ],
    deps = [
        "//java/com/google/common/flogger/android:android_log_tag",
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/delegatedui/api/common:common_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/common:nested_scroll",
        "//src/com/google/android/as/oss/delegatedui/api/infra/uiservice:parcelable_keys",
        "//src/com/google/android/as/oss/delegatedui/api/infra/uiservice:service_kt_grpc",
        "//src/com/google/android/as/oss/delegatedui/api/infra/uiservice:service_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress:egress_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/common",
        "//src/com/google/android/as/oss/delegatedui/service/renderer",
        "//src/com/google/android/as/oss/delegatedui/service/renderer:module",
        "//src/com/google/android/as/oss/delegatedui/utils:parcelable_over_rpc",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/core/ktx",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:androidx_core_core",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:javax_inject_javax_inject",
    ],
)
