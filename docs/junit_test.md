## junit_test

```Starlark
junit_test(name, **kwargs)
```

Builds JUnit 5 tests.

This is a drop-in replacement for [`java_test`](https://bazel.build/reference/be/java#java_test) that runs tests using a [JUnit 5 test runner](/tools/test-runner/README.md).

### Attributes

The attributes are the same as for [`java_test`](https://bazel.build/reference/be/java#java_test).
