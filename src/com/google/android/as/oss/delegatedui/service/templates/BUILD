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
    name = "templates",
    srcs = [
        "TemplateKey.kt",
        "TemplateRenderer.kt",
    ],
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    deps = [
        "//src/com/google/android/as/oss/delegatedui/api/common:common_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/infra/dataservice:data_service_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/egress:egress_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/api/integration/templates:templates_kt_proto_lite",
        "//src/com/google/android/as/oss/delegatedui/service/common",
        "//src/com/google/android/as/oss/delegatedui/service/data",
        "//src/com/google/android/as/oss/delegatedui/service/templates/scope",
        "//src/com/google/android/as/oss/delegatedui/utils:response_with_parcelables",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/core/ktx",
        "@maven//:com_google_dagger_dagger",
    ],
)
