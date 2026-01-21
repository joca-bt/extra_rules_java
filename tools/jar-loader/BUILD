load("@rules_java//java:java_library.bzl", "java_library")

java_library(
    name = "jar-loader",
    srcs = glob(["src/main/java/**"]),
    javacopts = [
        "-Xlint:-deprecation",
        "-Xlint:-removal",
        "-Xlint:-this-escape",
        "-Xlint:-try",
    ],
    visibility = ["//visibility:public"],
)
