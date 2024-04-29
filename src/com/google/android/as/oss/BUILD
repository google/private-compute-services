# Copyright 2024 Google LLC
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
        "//src/com/google/android/as/oss/common/config/noop:flags_impl_noop",
        "//src/com/google/android/as/oss/common/flavor:release",
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/common/time:system_module",
        "//src/com/google/android/as/oss/grpc:grpc_pcs",
        "//src/com/google/android/as/oss/grpc/impl:client",
        "//src/com/google/android/as/oss/grpc/impl:server",
        "//src/com/google/android/as/oss/grpc/perapp:pcs",
        "//src/com/google/android/as/oss/http/config/impl",
        "//src/com/google/android/as/oss/http/service",
        "//src/com/google/android/as/oss/logging/noop:impl_module",
        "//src/com/google/android/as/oss/networkusage/db/noop",
        "//src/com/google/android/as/oss/networkusage/ui/content/noop",
        "//third_party/java/hilt:hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)
