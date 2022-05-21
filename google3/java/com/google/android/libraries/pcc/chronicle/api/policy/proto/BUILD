load("//tools/build_defs/kotlin:rules.bzl", "kt_jvm_library")

package(
    default_visibility = [
        "//java/com/google/android/libraries/pcc/chronicle:policy_proto_visibility",
    ],
)

alias(
    name = "annotation_proto",
    actual = "//third_party/java/arcs:annotation_proto",
)

java_lite_proto_library(
    name = "annotation_java_proto_lite",
    deps = [":annotation_proto"],
)

java_proto_library(
    name = "annotation_java_proto",
    deps = [":annotation_proto"],
)

py_proto_library(
    name = "annotation_py_proto",
    api_version = 2,
    deps = [":annotation_proto"],
)

alias(
    name = "manifest_proto",
    actual = "//third_party/java/arcs:manifest_proto",
)

java_lite_proto_library(
    name = "manifest_java_proto_lite",
    deps = [":manifest_proto"],
)

java_proto_library(
    name = "manifest_java_proto",
    deps = [":manifest_proto"],
)

py_proto_library(
    name = "manifest_py_proto",
    api_version = 2,
    deps = [":manifest_proto"],
)

alias(
    name = "policy_proto",
    actual = "//third_party/java/arcs:policy_proto",
)

java_lite_proto_library(
    name = "policy_java_proto_lite",
    deps = [":policy_proto"],
)

java_proto_library(
    name = "policy_java_proto",
    deps = [":policy_proto"],
)

py_proto_library(
    name = "policy_py_proto",
    api_version = 2,
    deps = [":policy_proto"],
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
