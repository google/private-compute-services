load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(default_visibility = [
    "//java/com/google/android/libraries/pcc/chronicle:internal_visibility",
])

kt_jvm_library(
    name = "capabilities",
    srcs = [
        "Capabilities.kt",
        "Capability.kt",
    ],
    constraints = ["android"],
    deps = [
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
    ],
)
