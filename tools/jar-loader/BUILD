load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jar-loader",
    srcs = glob(["src/main/java/**/*.java"]),
    javacopts = [
        "-Xlint:-deprecation",
        "-Xlint:-this-escape",
        "-Xlint:-try",
    ],
    visibility = ["//visibility:public"],
)
