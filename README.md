# Additional Java rules for Bazel

Rules:

- [javadoc](#javadoc)

## Usage

```bazel
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "extra_rules_java",
    sha256 = <sha>,
    strip_prefix = "extra_rules_java-{}".format(<tag>),
    url = "https://github.com/joca-bt/extra_rules_java/archive/{}.zip".format(<tag>),
)
```

## Rules

### javadoc

```bazel
javadoc(name, exclude, javacopts, libs, links, packages, title)
```

Generate the Javadoc for a set of [libraries](https://bazel.build/reference/be/java#java_library).

**Arguments**

| Name      | Type            | Mandatory | Default                                                 |
| ---       | ---             | ---       | ---                                                     |
| name      | Name            | Yes       |                                                         |
| exclude   | List of strings | No        | []                                                      |
| javacopts | List of strings | No        | []                                                      |
| libs      | List of labels  | Yes       |                                                         |
| links     | List of strings | No        | ["https://docs.oracle.com/en/java/javase/17/docs/api/"] |
| packages  | List of strings | Yes       |                                                         |
| title     | String          | No        | ""                                                      |
