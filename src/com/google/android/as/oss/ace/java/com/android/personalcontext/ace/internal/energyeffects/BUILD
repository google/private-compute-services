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

package(
    default_visibility = [
        "//third_party/java/android_app/personalcontext:libraries",
    ],
)

android_library(
    name = "energyeffects",
    srcs = [
        "EnergyEffectsAnimationUtils.kt",
    ],
    deps = [
        "//java/com/google/android/libraries/material/compose/effect:effect_experimental_api",
        "//java/com/google/android/libraries/material/compose/energy:energy_base",
        "//java/com/google/android/libraries/material/gm3/color/tokens",
        "//java/com/google/android/systemui/graphics/shaderlib:compose",
        "//java/com/google/android/systemui/graphics/shaderlib:view",
        "//java/com/google/android/systemui/graphics/shaderlib/ane/colors",
        "//java/com/google/ux/material/libmonet/energy:energy_colors",
        "//third_party/java/androidx/compose/animation/animation_core",
        "//third_party/java/androidx/compose/foundation",
        "//third_party/java/androidx/compose/material3",
        "//third_party/java/androidx/compose/runtime",
        "//third_party/java/androidx/compose/ui",
        "//third_party/java/androidx/compose/ui/geometry",
        "//third_party/java/androidx/compose/ui/graphics",
        "//third_party/java/androidx/compose/ui/unit",
        "@maven//:androidx_core_core",
    ],
)
