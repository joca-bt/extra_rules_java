visibility("private")

_COMPILATION_TOOLCHAIN_TYPE = "@bazel_tools//tools/jdk:toolchain_type"

_EXECUTION_TOOLCHAIN_TYPE = "@bazel_tools//tools/jdk:runtime_toolchain_type"

def _compilation_toolchain(ctx):
    return ctx.toolchains[_COMPILATION_TOOLCHAIN_TYPE].java

def _compilation_jar(ctx):
    toolchain = _compilation_toolchain(ctx)

    for file in toolchain.java_runtime.files.to_list():
        if file.basename == "jar":
            return file

def _execution_toolchain(ctx):
    return ctx.toolchains[_EXECUTION_TOOLCHAIN_TYPE].java_runtime

def _execution_java(ctx):
    toolchain = _execution_toolchain(ctx)
    return toolchain.java_executable_runfiles_path

toolchains = struct(
    COMPILATION_TOOLCHAIN_TYPE = _COMPILATION_TOOLCHAIN_TYPE,
    EXECUTION_TOOLCHAIN_TYPE = _EXECUTION_TOOLCHAIN_TYPE,
    compilation_toolchain = _compilation_toolchain,
    compilation_jar = _compilation_jar,
    execution_toolchain = _execution_toolchain,
    execution_java = _execution_java,
)
