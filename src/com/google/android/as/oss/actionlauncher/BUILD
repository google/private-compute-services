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
    name = "action_launcher_activity_prod",
    srcs = ["ActionLauncherActivity.kt"],
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    resource_files = [
        "res/values/config.xml",
        "res/values/styles.xml",
    ],
    deps = [
        ":constants",
        "//java/com/google/common/flogger/android:android_log_tag",
        "//src/com/google/android/as/oss/actionlauncher/config",
        "//src/com/google/android/as/oss/availability/api/agenticintegration:agentic_integration_service_api",
        "//src/com/google/android/as/oss/availability/api/agenticintegration:error_code_kt_proto_lite",
        "//src/com/google/android/as/oss/availability/service/impl:agentic_integration_service_grpc_client_module",
        "//src/com/google/android/as/oss/common",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/security:security_policy_utils",
        "//src/com/google/android/as/oss/common/security/config",
        "//src/com/google/android/as/oss/common/time",
        "//third_party/java/androidx/appcompat",
        "//third_party/java/protobuf/util:time_lite",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:io_grpc_grpc_api",
        "@maven//:io_grpc_grpc_protobuf_lite",
        "@maven//:javax_inject_javax_inject",
    ],
)

android_library(
    name = "constants",
    srcs = ["Constants.kt"],
)

android_library(
    name = "prod_modules",
    tags = [
        "keep_dep",
    ],
    exports = [
        ":action_launcher_activity_prod",
        "//src/com/google/android/as/oss/actionlauncher/config:module",
    ],
)
