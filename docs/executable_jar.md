## executable_jar

```Starlark
executable_jar(name, main_class, libs)
```

Builds an [executable jar](/tools/jar-loader/README.md) from a set of [libraries](https://bazel.build/reference/be/java#java_library).

### Attributes

| Name       | Type           | Mandatory |
| ---        | ---            | ---       |
| name       | Name           | Yes       |
| main_class | String         | Yes       |
| libs       | List of labels | Yes       |
