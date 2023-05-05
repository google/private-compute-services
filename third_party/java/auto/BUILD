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

load("@bazel_rules_android//android:rules.bzl", "android_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "auto_value",
    exported_plugins = [
        ":auto_annotation_plugin",
        ":auto_builder_plugin",
        ":auto_oneof_plugin",
        ":auto_value_plugin",
        ":auto_value_builder_plugin",
    ],
    exports = [
        "@maven//:com_google_auto_value_auto_value",
        "@maven//:com_google_auto_value_auto_value_annotations",
    ],
)

java_plugin(
    name = "auto_builder_plugin",
    processor_class = "com.google.auto.value.processor.AutoBuilderProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoBuilder;" +
        "genclass=${package}.AutoBuilder_${outerclasses}${classname}",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_oneof_plugin",
    processor_class = "com.google.auto.value.processor.AutoOneOfProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoOneOf;" +
        "genclass=${package}.AutoOneOf_${outerclasses}${classname}",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_annotation_plugin",
    processor_class = "com.google.auto.value.processor.AutoAnnotationProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoAnnotation;" +
        "genclass=${package}.AutoAnnotation_${outerclasses}${classname}_${methodname}",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_value_plugin",
    processor_class = "com.google.auto.value.processor.AutoValueProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoValue;" +
        "genclass=${package}.AutoValue_${outerclasses}${classname}",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_value_builder_plugin",
    processor_class = "com.google.auto.value.processor.AutoValueBuilderProcessor",
    tags = [
        "annotation=com.google.auto.value.AutoValue.Builder;",
    ],
    deps = [
        "@maven//:com_google_auto_value_auto_value",
    ],
)

java_plugin(
    name = "auto_value_parcel",
    generates_api = 1,
    deps = [
        "@maven//:com_ryanharter_auto_value_auto_value_parcel",
    ],
)

android_library(
    name = "auto_value_parcel_adapter",
    exports = ["@maven//:com_ryanharter_auto_value_auto_value_parcel_adapter"],
)
