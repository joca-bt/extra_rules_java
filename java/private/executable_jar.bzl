load("@rules_java//java:defs.bzl", "JavaInfo")

def _executable_jar_impl(ctx):
    output = ctx.actions.declare_file("{}.jar".format(ctx.label.name))

    wd = "{}/".format(ctx.label.name)
    jdk = ctx.toolchains["@bazel_tools//tools/jdk:toolchain_type"].java.java_runtime
    jar = [file for file in jdk.files.to_list() if file.basename == "jar"][0]

    jars = depset(transitive = [lib[JavaInfo].transitive_runtime_jars for lib in ctx.attr.libs]).to_list()
    jar_loader = ctx.file._jar_loader
    manifest = """\
Main-Class: extrarulesjava.jarloader.JarLoader
Start-Class: {}\
""".format(ctx.attr.main_class)

    # Use the same timestamp as singlejar.
    timestamp = "2010-01-01T00:00:00-00:00"

    commands = [
        "mkdir {0}/ {0}/META-INF/ {0}/extrarulesjava/ {0}/jars/".format(wd),
        "cp {} {}/jars/".format(" ".join([jar.path for jar in jars]), wd),
        "(cd {}/ && ../{} -x -f ../{} extrarulesjava/)".format(wd, jar.path, jar_loader.path),
        "echo '{}' > {}/META-INF/MANIFEST.MF".format(manifest, wd),
        "{0} -c -0 --date={1} -f {2} -m {3}/META-INF/MANIFEST.MF -C {3}/ .".format(jar.path, timestamp, output.path, wd),
    ]

    ctx.actions.run_shell(
        command = " && ".join(commands),
        inputs = jars + [jar_loader],
        outputs = [output],
        tools = [jar],
    )

    return [DefaultInfo(files = depset([output]))]

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
    },
    toolchains = [
        "@bazel_tools//tools/jdk:toolchain_type",
    ],
)
