load("@rules_java//java/common:java_info.bzl", "JavaInfo")
load(":toolchains.bzl", "toolchains")

def _executable_jar_impl(ctx):
    jar = _build_jar(ctx)
    script = _build_script(ctx, jar)

    files = depset([jar])
    executable = script
    runfiles = ctx.runfiles(files = [jar], transitive_files = toolchains.execution_toolchain(ctx).files)

    return [DefaultInfo(files = files, executable = executable, runfiles = runfiles)]

def _build_jar(ctx):
    output = ctx.actions.declare_file("{}.jar".format(ctx.label.name))

    wd = "{}/".format(ctx.label.name)
    jar = toolchains.compilation_jar(ctx)

    manifest = """\
Main-Class: extrarulesjava.jarloader.JarLoader
Start-Class: {}\
""".format(ctx.attr.main_class)
    jar_loader = ctx.file._jar_loader
    jars = depset(transitive = [lib[JavaInfo].transitive_runtime_jars for lib in ctx.attr.libs]).to_list()

    # Use the same timestamp as singlejar.
    timestamp = "2010-01-01T00:00:00-00:00"

    commands = [
        "mkdir {0}/ {0}/META-INF/ {0}/extrarulesjava/ {0}/jars/".format(wd),
        "echo '{}' > {}/META-INF/MANIFEST.MF".format(manifest, wd),
        "(cd {}/ && ../{} -x -f ../{} extrarulesjava/)".format(wd, jar.path, jar_loader.path),
        "cp {} {}/jars/".format(" ".join([jar.path for jar in jars]), wd),
        "{0} -c -0 --date={1} -f {2} -m {3}/META-INF/MANIFEST.MF -C {3}/ .".format(jar.path, timestamp, output.path, wd),
    ]

    ctx.actions.run_shell(
        command = " && ".join(commands),
        inputs = [jar_loader] + jars,
        outputs = [output],
        tools = [jar],
    )

    return output

def _build_script(ctx, jar):
    output = ctx.actions.declare_file("{}.sh".format(ctx.label.name))

    ctx.actions.expand_template(
        template = ctx.file._wrapper,
        output = output,
        substitutions = {
            "JAVA=": "JAVA='{}'".format(toolchains.execution_java(ctx)),
            "JAR=": "JAR='{}'".format(jar.short_path),
        },
        is_executable = True,
    )

    return output

executable_jar = rule(
    doc = "Builds an executable jar from a set of libraries.",
    implementation = _executable_jar_impl,
    attrs = {
        "main_class": attr.string(
            mandatory = True,
        ),
        "libs": attr.label_list(
            mandatory = True,
            allow_empty = False,
            providers = [JavaInfo],
        ),
        "_jar_loader": attr.label(
            default = "//tools/jar-loader",
            allow_single_file = True,
        ),
        "_wrapper": attr.label(
            default = ":wrapper.sh",
            allow_single_file = True,
        ),
    },
    toolchains = [
        toolchains.COMPILATION_TOOLCHAIN_TYPE,
        toolchains.EXECUTION_TOOLCHAIN_TYPE,
    ],
    executable = True,
)
