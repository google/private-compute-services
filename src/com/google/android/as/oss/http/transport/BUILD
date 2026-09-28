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
    name = "transport",
    srcs = [
        "PcsHttpConnection.java",
        "PcsHttpDownloaderTransport.java",
        "PcsHttpDownloaderTransportModule.java",
    ],
    manifest = "//src/com/google/android/as/oss/common:AndroidManifest.xml",
    deps = [
        "//java/com/google/android/libraries/net/downloader",
        "//java/com/google/android/libraries/net/downloader:cronet",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/grpc:status",
        "//src/com/google/android/as/oss/http/api:http_java_proto_lite",
        "//src/com/google/android/as/oss/http/client",
        "//src/com/google/android/as/oss/http/config",
        "//third_party/java/android_libs/cronet:cronet_api",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_api_grpc_proto_google_common_protos",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_checkerframework_checker_qual",
    ],
)

android_library(
    name = "url_engine",
    srcs = [
        "PcsUrlEngine.java",
    ],
    deps = [
        ":transport",
        "//src/com/google/android/as/oss/common/config",
        "//src/com/google/android/as/oss/http/client",
        "//src/com/google/android/as/oss/http/config",
        "//third_party/java/android_libs/downloader:urlengine",
        "//third_party/java/android_libs/guava_jdk5:net",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
    ],
)
