package extrarulesjava.testrunner;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.platform.commons.JUnitException;

class Arguments {
    public static String[] getArguments(Path jar, String filter, Path report) {
        List<String> arguments = new ArrayList<>();

        arguments.add("execute");
        arguments.add("--details=none");
        arguments.add("--disable-banner");
        arguments.add("--fail-if-no-tests");
        arguments.add("--reports-dir=%s".formatted(report.getParent()));
        addSelectors(arguments, jar, filter);

        return arguments.toArray(new String[0]);
    }

    /**
     * Adds selectors based on the --test_filter option.
     *
     * The --test_filter option specifies which tests to run. This option is provided to the test
     * runner through the TESTBRIDGE_TEST_ONLY environment variable.
     */
    private static void addSelectors(List<String> arguments, Path jar, String filter) {
        if (filter == null) {
            arguments.add("--scan-classpath=%s".formatted(jar));
            return;
        }

        // Adjust for IntelliJ IDEA.
        filter = filter.replaceAll("[$\\\\]", "");

        for (var component : filter.split("\\|(?![^()]+\\))")) {
            String[] names = component.split("#");

            Class<?> clazz = findClass(names[0]);

            if (names.length == 1) {
                arguments.add("--select-class=%s".formatted(clazz.getName()));
                continue;
            }

            for (var spec : names[1].split("\\|")) {
                // Clean up.
                spec = spec.replaceAll("^\\(|\\)$|(?<=]).+", "");

                String[] elements = spec.split("(?=\\[)");

                Method method = findMethod(clazz, elements[0]);
                String parameters = Arrays.stream(method.getParameterTypes())
                    .map(Class::getName)
                    .collect(Collectors.joining(","));

                if (elements.length == 1) {
                    arguments.add("--select-method=%s#%s(%s)".formatted(clazz.getName(), method.getName(), parameters));
                    continue;
                }

                String iterations = Pattern.compile("\\d+")
                    .matcher(elements[1])
                    .replaceAll(match -> Integer.toString(Integer.parseInt(match.group()) - 1));

                arguments.add("--select-iteration=method:%s#%s(%s)%s".formatted(clazz.getName(), method.getName(), parameters, iterations));
            }
        }
    }

    private static Class<?> findClass(String name) {
        while (true) {
            try {
                return Class.forName(name);
            } catch (Exception exception) {
                int i = name.lastIndexOf('.');

                if (i == -1) {
                    String message = "Could not find class %s.".formatted(name.replace('$', '.'));
                    throw new JUnitException(message);
                }

                name = "%s$%s".formatted(name.substring(0, i), name.substring(i + 1));
            }
        }
    }

    private static Method findMethod(Class<?> clazz, String name) {
        List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
            .filter(method -> method.getName().equals(name))
            .toList();

        if (methods.size() != 1) {
            String message = "Could not find method %s#%s.".formatted(clazz.getName().replace('$', '.'), name);
            throw new JUnitException(message);
        }

        return methods.getFirst();
    }
}
