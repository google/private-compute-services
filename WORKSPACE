# Copyright 2023 Google LLC
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

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# Pull down rules_jvm_external for integrateing external dependencies
RULES_JVM_EXTERNAL_TAG = "4.5"

RULES_JVM_EXTERNAL_SHA = "b17d7388feb9bfa7f2fa09031b32707df529f26c91ab9e5d909eb1676badd9a6"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

# Load dagger/hilt repository
DAGGER_TAG = "2.44.2"

DAGGER_SHA = "cbff42063bfce78a08871d5a329476eb38c96af9cf20d21f8b412fee76296181"

http_archive(
    name = "dagger",
    sha256 = DAGGER_SHA,
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG],
)

load(
    "@dagger//:workspace_defs.bzl",
    "HILT_ANDROID_ARTIFACTS",
    "HILT_ANDROID_REPOSITORIES",
)
load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = HILT_ANDROID_ARTIFACTS + [
        "com.google.auto.value:auto-value:1.10.1",
        "com.google.auto.value:auto-value-annotations:1.10.1",
        "com.ryanharter.auto.value:auto-value-parcel:0.2.9",
        "com.ryanharter.auto.value:auto-value-parcel-adapter:0.2.9",
        "com.google.dagger:dagger:2.44.2",
        "javax.inject:javax.inject:1",
        "com.google.guava:guava:31.1-android",
        "androidx.annotation:annotation:1.5.0",
        "androidx.lifecycle:lifecycle-service:2.5.1",
        "androidx.lifecycle:lifecycle-livedata:2.5.1",
        "androidx.preference:preference:1.2.0",
        "androidx.fragment:fragment:1.5.1",
        "androidx.fragment:fragment-ktx:1.5.1",
        "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0",
        "androidx.activity:activity:1.6.1",
        "androidx.activity:activity-ktx:1.6.1",
        "com.google.errorprone:error_prone_annotations:2.17.0",
        "androidx.core:core:1.9.0",
        "androidx.room:room-runtime:2.4.3",
        "androidx.lifecycle:lifecycle-livedata-core:2.5.1",
        "com.google.api.grpc:proto-google-common-protos:2.12.0",
        "io.grpc:grpc-protobuf-lite:1.51.1",
        "io.grpc:grpc-netty-shaded:1.51.1",
        "io.grpc:grpc-binder:1.51.1",
        "io.grpc:grpc-core:1.51.1",
        "io.grpc:grpc-stub:1.51.1",
        "io.grpc:grpc-context:1.51.1",
        "com.google.protobuf:protobuf-lite:3.0.1",
        "com.squareup.okhttp3:okhttp:4.10.0",
        "com.google.flogger:google-extensions:0.7.4",
    ],
    fetch_sources = True,
    repositories = HILT_ANDROID_REPOSITORIES + [
        "https://jcenter.bintray.com/",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
        "https://plugins.gradle.org/m2/",
    ],
)

# Pull down rules to generate a Java protobuf and gRPC library using java_library
http_archive(
    name = "rules_proto_grpc",
    sha256 = "28724736b7ff49a48cb4b2b8cfa373f89edfcb9e8e492a8d5ab60aa3459314c8",
    strip_prefix = "rules_proto_grpc-4.0.1",
    urls = ["https://github.com/rules-proto-grpc/rules_proto_grpc/archive/4.0.1.tar.gz"],
)

load("@rules_proto_grpc//:repositories.bzl", "rules_proto_grpc_repos", "rules_proto_grpc_toolchains")

rules_proto_grpc_toolchains()

rules_proto_grpc_repos()

load("@rules_proto_grpc//java:repositories.bzl", rules_proto_grpc_java_repos = "java_repos")

rules_proto_grpc_java_repos()

load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_ARTIFACTS", "IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS", "grpc_java_repositories")

maven_install(
    name = "java_grpc",
    artifacts = IO_GRPC_GRPC_JAVA_ARTIFACTS,
    generate_compat_repositories = True,
    override_targets = IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS,
    repositories = [
        "https://repo.maven.apache.org/maven2/",
    ],
)

load("@java_grpc//:compat.bzl", "compat_repositories")

compat_repositories()

grpc_java_repositories()

android_sdk_repository(
    name = "androidsdk",
    api_level = 33,
)

# Allows us to use android_library and android_binary rules.
http_archive(
    name = "bazel_rules_android",
    sha256 = "cd06d15dd8bb59926e4d65f9003bfc20f9da4b2519985c27e190cddc8b7a7806",
    strip_prefix = "rules_android-0.1.1",
    urls = ["https://github.com/bazelbuild/rules_android/archive/v0.1.1.zip"],
)

# Allow dependency on policy_java_proto_lite from private compute libraries.
RULES_KOTLIN_VERSION = "1.7.1"

RULES_KOTLIN_SHA = "fd92a98bd8a8f0e1cdcb490b93f5acef1f1727ed992571232d33de42395ca9b3"

# Allows us to use kt_android_library, kt_jvm_library, and friends.
http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = RULES_KOTLIN_SHA,
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/v%s/rules_kotlin_release.tgz" % RULES_KOTLIN_VERSION],
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")

kotlin_repositories()

load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")

kt_register_toolchains()

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name = "private_compute_libraries",
    remote = "https://github.com/google/private-compute-libraries.git",
    tag = "v0.1.0-20230105",
)
