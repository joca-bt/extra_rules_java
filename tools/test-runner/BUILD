load("@rules_java//java:java_library.bzl", "java_library")

java_library(
    name = "test-runner",
    srcs = glob(["src/main/java/**"]),
    deps = [
        "@extra_rules_java_maven//:org_junit_platform_junit_platform_console_standalone",
    ],
    visibility = ["//visibility:public"],
)
