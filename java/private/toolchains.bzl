visibility("private")

_COMPILATION_TOOLCHAIN_TYPE = "@bazel_tools//tools/jdk:toolchain_type"

def _compilation_toolchain(ctx):
    return ctx.toolchains[_COMPILATION_TOOLCHAIN_TYPE].java

def _compilation_jar(ctx):
    toolchain = _compilation_toolchain(ctx)

    for file in toolchain.java_runtime.files.to_list():
        if file.basename == "jar":
            return file

toolchains = struct(
    COMPILATION_TOOLCHAIN_TYPE = _COMPILATION_TOOLCHAIN_TYPE,
    compilation_toolchain = _compilation_toolchain,
    compilation_jar = _compilation_jar,
)
