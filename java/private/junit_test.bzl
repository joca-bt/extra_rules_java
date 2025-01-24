load("@rules_java//java/bazel/rules:bazel_java_test.bzl", "java_test")

def _junit_test_impl(name, **kwargs):
    kwargs["runtime_deps"] = (kwargs["runtime_deps"] or []) + ["@extra_rules_java//tools/test-runner"]
    jar = "{}/{}.jar".format(native.package_name(), name)

    java_test(
        name = name,
        main_class = "extrarulesjava.testrunner.TestRunner",
        use_testrunner = False,
        args = [
            jar,
        ],
        **kwargs,
    )

junit_test = macro(
    doc = "Builds JUnit 5 tests.",
    implementation = _junit_test_impl,
    inherit_attrs = java_test,
    attrs = {
        "main_class": None,
        "use_testrunner": None,
        "args": None,
    },
)
