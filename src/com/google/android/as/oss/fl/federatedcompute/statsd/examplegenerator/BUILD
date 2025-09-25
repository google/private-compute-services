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
    name = "example_generator",
    srcs = glob(["*.kt"]),
    deps = [
        ":tff_feature_creator",
        "//third_party/tensorflow/core/example:example_protos_java_proto_lite",
    ],
)

android_library(
    name = "tff_feature_creator",
    srcs = ["TFFeatureCreator.java"],
    deps = [
        "//third_party/tensorflow/core/example:example_protos_java_proto_lite",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_protobuf_protobuf_javalite",
    ],
)
