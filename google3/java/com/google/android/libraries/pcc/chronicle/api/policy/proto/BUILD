load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(
    default_visibility = [
        "//java/com/google/android/libraries/pcc/chronicle:policy_proto_visibility",
    ],
)

java_lite_proto_library(
    name = "annotation_java_proto_lite",
    deps = ["//third_party/java/arcs:annotation_proto"],
)

java_proto_library(
    name = "annotation_java_proto",
    deps = ["//third_party/java/arcs:annotation_proto"],
)

java_lite_proto_library(
    name = "manifest_java_proto_lite",
    deps = ["//third_party/java/arcs:manifest_proto"],
)

java_proto_library(
    name = "manifest_java_proto",
    deps = ["//third_party/java/arcs:manifest_proto"],
)

java_lite_proto_library(
    name = "policy_java_proto_lite",
    deps = ["//third_party/java/arcs:policy_proto"],
)

java_proto_library(
    name = "policy_java_proto",
    deps = ["//third_party/java/arcs:policy_proto"],
)

kt_jvm_library(
    name = "proto",
    srcs = glob(["*.kt"]),
    constraints = ["android"],
    deps = [
        ":annotation_java_proto_lite",
        ":policy_java_proto_lite",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
    ],
)

kt_jvm_library(
    name = "proto-jvm",
    srcs = glob(["*.kt"]),
    deps = [
        ":annotation_java_proto",
        ":policy_java_proto",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy",
        "//java/com/google/android/libraries/pcc/chronicle/api/policy/annotation",
    ],
)
