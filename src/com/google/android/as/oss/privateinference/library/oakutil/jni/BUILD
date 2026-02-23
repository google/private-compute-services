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

load("//mobile/build:build_defs.bzl", "android_jni_library")
load("//third_party/bazel_rules/rules_rust/rust:defs.bzl", "rust_library")

package(
    default_applicable_licenses = ["//third_party/oak:license"],
    default_visibility = ["//security/oak:default_visibility"],
    licenses = ["notice"],
)

rust_library(
    name = "pi_client_session_config_rust",
    srcs = ["pi_client_session_config_jni.rs"],
    edition = "2021",
    deps = [
        "//third_party/oak/java/src/main/java/com/google/oak/remote_attestation/jni:oak_jni_attestation_verification_clock",
        "//third_party/oak/java/src/main/java/com/google/oak/session/jni:oak_jni_attestation_publisher",
        "//third_party/oak/oak_attestation_verification",
        "//third_party/oak/oak_attestation_verification_types",
        "//third_party/oak/oak_crypto",
        "//third_party/oak/oak_crypto:oak_crypto_tink",
        "//third_party/oak/oak_proto_rust",
        "//third_party/oak/oak_session",  # buildcleaner: keep
        "//third_party/oak/oak_time",
        "//third_party/rust/anyhow/v1:anyhow",  # buildcleaner: keep
        "//third_party/rust/jni/v0_21:jni",  # buildcleaner: keep
    ],
    alwayslink = 1,
)

android_jni_library(
    name = "pi_client_session_config_jni",
    binary_name = "libpi_client_session_config_jni.so",
    deps = [
        ":pi_client_session_config_rust",
        "//devtools/rust:rust_okay_here",
        "//src/com/google/android/as/oss/privateinference/library/bsa/jni:blind_sign_auth_jni_lib",
        "//third_party/oak/java/src/main/java/com/google/oak/session/jni:oak_client_session_rust",
    ],
    alwayslink = 1,
)
