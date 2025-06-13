visibility("private")

def compilation_toolchain(ctx):
    return ctx.toolchains["@bazel_tools//tools/jdk:toolchain_type"].java

def compilation_jar(ctx):
    toolchain = compilation_toolchain(ctx)

    for file in toolchain.java_runtime.files.to_list():
        if file.basename == "jar":
            return file
