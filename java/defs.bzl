def _create_jar(jar, dir, java_home):
    return "{}/bin/jar -c -f {} -C {} .".format(java_home, jar, dir)

def _extract_jars(jars, dir):
    return " && ".join(["unzip -q -o {} -d {}".format(jar, dir) for jar in jars])

def _get_paths(depset):
    return [element.path for element in depset.to_list()]

def _javadoc_impl(ctx):
    srcs = depset(transitive = [depset(lib[JavaInfo].source_jars) for lib in ctx.attr.libs])
    deps = depset(transitive = [lib[JavaInfo].transitive_compile_time_jars for lib in ctx.attr.libs])
    jar = ctx.actions.declare_file("{}.jar".format(ctx.label.name))

    java_home = ctx.attr._jdk[java_common.JavaRuntimeInfo].java_home
    tmp_dir = "{}/_{}/".format(jar.dirname, jar.basename)
    src_dir = "{}/src/".format(tmp_dir)
    javadoc_dir = "{}/javadoc/".format(tmp_dir)

    # https://docs.oracle.com/en/java/javase/17/docs/specs/man/javadoc.html
    javadoc = [
        "{}/bin/javadoc".format(java_home),
        "-quiet",
        "-Xdoclint:-missing",
        "--class-path {}".format(":".join(_get_paths(deps))),
        "--source-path {}".format(src_dir),
        "-subpackages {}".format(":".join(ctx.attr.packages)),
        "-d {}".format(javadoc_dir),
        "-encoding UTF-8",
        "-docencoding UTF-8",
        "-notimestamp",
    ]

    if ctx.attr.exclude:
        javadoc.append("-exclude {}".format(":".join(ctx.attr.exclude)))

    javadoc.extend(ctx.attr.javacopts)

    for link in ctx.attr.links:
        javadoc.append("-link {}".format(link))

    if ctx.attr.title:
        javadoc.append("-doctitle '{}'".format(ctx.attr.title))
        javadoc.append("-windowtitle '{}'".format(ctx.attr.title))

    cmds = [
        "rm -rf {}".format(tmp_dir),
        "mkdir {}".format(tmp_dir),
        _extract_jars(_get_paths(srcs), src_dir),
        " ".join(javadoc),
        _create_jar(jar.path, javadoc_dir, java_home),
    ]

    ctx.actions.run_shell(
        command = " && ".join(cmds),
        inputs = ctx.files._jdk + srcs.to_list() + deps.to_list(),
        outputs = [jar],
    )

    return [DefaultInfo(files = depset([jar]))]

javadoc = rule(
    doc = "Generate the Javadoc for a set of libraries.",
    implementation = _javadoc_impl,
    attrs = {
        "exclude": attr.string_list(),
        "javacopts": attr.string_list(),
        "libs": attr.label_list(
            allow_empty = False,
            mandatory = True,
            providers = [JavaInfo],
        ),
        "links": attr.string_list(
            default = ["https://docs.oracle.com/en/java/javase/17/docs/api/"],
        ),
        "packages": attr.string_list(
            allow_empty = False,
            mandatory = True,
        ),
        "title": attr.string(),
        "_jdk": attr.label(
            default = Label("@bazel_tools//tools/jdk:current_host_java_runtime"),
            providers = [java_common.JavaRuntimeInfo],
        ),
    },
)
