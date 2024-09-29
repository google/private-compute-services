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

load("@rules_proto_grpc//java:defs.bzl", "java_grpc_library")

package(default_visibility = ["//visibility:public"])

proto_library(
    name = "http_proto",
    srcs = ["http.proto"],
    has_services = True,
)

java_lite_proto_library(
    name = "http_java_proto_lite",
    deps = [":http_proto"],
)

java_grpc_library(
    name = "http_grpc",
    constraints = ["android"],
    flavor = "lite",
    protos = [":http_proto"],
    deps = [
        ":http_java_proto_lite",
    ],
)
