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
    name = "quartz_service_client",
    srcs = [
        "QuartzFeedbackDataServiceClient.kt",
        "QuartzFeedbackDataServiceClientImpl.kt",
        "QuartzFeedbackDataServiceClientModule.kt",
    ],
    deps = [
        "//google/protobuf:timestamp_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback:view_feedback_data",
        "//src/com/google/android/as/oss/feedback/proto:feedback_tag_data_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/dataservice:data_service_kt_grpc",
        "//src/com/google/android/as/oss/feedback/proto/dataservice:data_service_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:quartz_log_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/proto/gateway:spoon_kt_proto_lite",
        "//src/com/google/android/as/oss/feedback/serviceclient/serviceconnection:quartz_annotations",
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_hilt-android",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:javax_inject_javax_inject",
    ],
)
