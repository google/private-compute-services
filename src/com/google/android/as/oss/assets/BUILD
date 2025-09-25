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

# Root for policies
# This filegroup aggregates all policy-related source files, build definitions,
# and documentation for open-source release.
filegroup(
    name = "policy_source_files_for_release",
    srcs = glob(
        [
            "**/*.kt",
            "**/*.bzl",
            "**/BUILD",
            "**/*.md",
        ],
        exclude = [
            "**/internal/**",
            "**/archive/**",
            "**/*.binarypb",
        ],
    ) + [
        "//src/com/google/android/as/oss/assets/federatedcompute:federatedcompute_sources_for_release",
    ],
    visibility = ["//src/com/google/android/as/oss:__pkg__"],
)
