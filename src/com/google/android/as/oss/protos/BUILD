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

package(default_visibility = ["//visibility:public"])

proto_library(
    name = "pcs_feature_enum_proto",
    srcs = [
        "pcs_feature-enum.proto",
    ],
)

java_lite_proto_library(
    name = "pcs_feature_enum_java_proto_lite",
    deps = [":pcs_feature_enum_proto"],
)

proto_library(
    name = "pcs_statsquery_proto",
    srcs = [
        "pcs_statsquery.proto",
    ],
)

java_lite_proto_library(
    name = "pcs_statsquery_java_proto_lite",
    deps = [":pcs_statsquery_proto"],
)

proto_library(
    name = "pcs_query_proto",
    srcs = [
        "pcs_query.proto",
    ],
    deps = [
        ":pcs_feature_enum_proto",
        "@com_google_protobuf//:any_proto",
        "@private_compute_libraries//java/com/google/android/libraries/pcc/chronicle/api/policy/proto:policy_proto",
    ],
)

java_lite_proto_library(
    name = "pcs_query_java_proto_lite",
    deps = [":pcs_query_proto"],
)
