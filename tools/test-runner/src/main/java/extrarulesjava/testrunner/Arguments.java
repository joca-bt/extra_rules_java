package extrarulesjava.testrunner;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.ReflectionSupport;

class Arguments {
    public static String[] getArguments(Path jar, String filter, Path report) {
        List<String> arguments = new ArrayList<>();

        arguments.add("execute");
        arguments.add("--disable-banner");
        arguments.add("--details=none");
        arguments.add("--fail-if-no-tests");
        addSelectors(arguments, jar, filter);
        arguments.add("--reports-dir=%s".formatted(report.getParent()));

        return arguments.toArray(new String[0]);
    }

    /**
     * Adds selectors based on the --test_filter option.
     *
     * The --test_filter option specifies which tests to run and is passed to the test runner
     * through the TESTBRIDGE_TEST_ONLY environment variable.
     */
    private static void addSelectors(List<String> arguments, Path jar, String filter) {
        if (filter == null) {
            arguments.add("--scan-classpath=%s".formatted(jar));
            return;
        }

        // Remove artifacts added by IntelliJ IDEA.
        filter = filter.replaceAll("\\$", "");

        for (var selector : filter.split(",")) {
            String[] components = selector.split("[.\\[\\]]");

            Class<?> clazz = findClass(jar, components[0]);

            if (components.length == 1) {
                arguments.add("--select-class=%s".formatted(clazz.getName()));
                continue;
            }

            Method method = findMethod(clazz, components[1]);
            String parameters = getParameters(method);

            if (components.length == 2) {
                arguments.add("--select-method=%s#%s(%s)".formatted(clazz.getName(), method.getName(), parameters));
                continue;
            }

            int iteration = Integer.parseInt(components[2]) - 1;

            arguments.add("--select-iteration=method:%s#%s(%s)[%d]".formatted(clazz.getName(), method.getName(), parameters, iteration));
        }
    }

    private static Class<?> findClass(Path jar, String name) {
        List<Class<?>> matches = ReflectionSupport.findAllClassesInClasspathRoot(
                jar.toUri(),
                clazz -> clazz.getSimpleName().equals(name),
                __ -> true);

        if (matches.size() != 1) {
            String message = "Could not find class %s.".formatted(name);
            throw new JUnitException(message);
        }

        return matches.getFirst();
    }

    private static Method findMethod(Class<?> clazz, String name) {
        List<Method> matches = Arrays.stream(clazz.getDeclaredMethods())
            .filter(method -> method.getName().equals(name))
            .toList();

        if (matches.size() != 1) {
            String message = "Could not find method %s.%s.".formatted(clazz.getSimpleName(), name);
            throw new JUnitException(message);
        }

        return matches.getFirst();
    }

    private static String getParameters(Method method) {
        return Arrays.stream(method.getParameterTypes())
            .map(Class::getName)
            .collect(Collectors.joining(","));
    }
}
