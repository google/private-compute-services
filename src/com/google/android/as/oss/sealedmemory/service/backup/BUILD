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

package(default_visibility = ["//visibility:public"])

proto_library(
    name = "sealed_memory_backup_proto",
    srcs = ["proto/sealed_memory_backup.proto"],
)

kt_jvm_lite_proto_library(
    name = "sealed_memory_backup_kt_proto_lite",
    deps = [":sealed_memory_backup_proto"],
)

android_library(
    name = "sealed_memory_backup_agent",
    srcs = ["SealedMemoryBackupAgent.kt"],
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    visibility = ["//visibility:public"],
    deps = [
        ":sealed_memory_backup_kt_proto_lite",
        "//src/com/google/android/as/oss/sealedmemory/service/impl:app_search_key_store",
        "//src/com/google/android/as/oss/sealedmemory/service/impl:encrypted_key",
        "//src/com/google/android/as/oss/sealedmemory/service/impl:encryption_result",
        "//src/com/google/android/as/oss/sealedmemory/service/impl:key_generation_util",
        "//third_party/java/dagger/hilt:entry_point",
        "//third_party/kotlin/kotlinx_coroutines:kotlinx_coroutines-jvm",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)

android_library(
    name = "sealed_memory_backup_module",
    srcs = ["SealedMemoryBackupModule.kt"],
    deps = [
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:javax_inject_javax_inject",
    ],
)
