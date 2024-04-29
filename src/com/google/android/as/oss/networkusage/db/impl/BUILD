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

load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

android_library(
    name = "impl",
    srcs = glob(["*.java"]) + [
        # These are required due to a Room bug.
        "//src/com/google/android/as/oss/networkusage/db:ConnectionDetails.java",
        "//src/com/google/android/as/oss/networkusage/db:NetworkUsageEntity.java",
    ],
    exports_manifest = 1,
    javacopts = ["-Aroom.schemaLocation=src/com/google/android/as/oss/networkusage/db/impl/schemas"],
    manifest = "AndroidManifest.xml",
    plugins = [
        "//third_party/java/auto:auto_value_parcel",
        ":schemas_plugin",  # For auto migrations
    ],
    deps = [
        "//src/com/google/android/as/oss/common:annotation",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/common/initializer",
        "//src/com/google/android/as/oss/networkusage/api:connection_key_java_proto_lite",
        "//src/com/google/android/as/oss/networkusage/config",
        "//src/com/google/android/as/oss/networkusage/db",
        "//src/com/google/android/as/oss/networkusage/db:repository",
        "//src/com/google/android/as/oss/networkusage/ui/content",
        "//src/com/google/android/as/oss/networkusage/ui/user:res_lib",
        "//third_party/java/auto:auto_value",
        "//third_party/java/auto:auto_value_parcel_adapter",
        "//third_party/java/hilt:hilt-android",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_lifecycle_lifecycle_livedata",
        "@maven//:androidx_lifecycle_lifecycle_livedata_core",
        "@maven//:androidx_preference_preference",
        "@maven//:androidx_room_room_common",
        "@maven//:androidx_room_room_runtime",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
        "@private_compute_libraries//java/com/google/android/libraries/pcc/chronicle/api/policy/proto:policy_java_proto_lite",
    ],
)

java_plugin(
    name = "schemas_plugin",
    data = glob(["schemas/**/*.json"]),
)
