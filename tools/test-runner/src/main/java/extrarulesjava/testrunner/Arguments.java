package extrarulesjava.testrunner;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        filter = filter.replaceAll("\\$$", "");

        for (var selector : filter.split(",")) {
            // Split on the last . and on [].
            String[] components = selector.split("\\.(?![^.]*\\.)|[\\[\\]]");

            Class<?> clazz = findClass(jar, selector);

            if (clazz != null) {
                arguments.add("--select-class=%s".formatted(clazz.getName()));
                continue;
            }

            clazz = findClass(jar, components[0]);

            if (clazz == null) {
                String message = "Unable to resolve selector %s.".formatted(selector);
                throw new JUnitException(message);
            }

            Method method = findMethod(clazz, components[1]);

            if (method == null) {
                String message = "Unable to resolve selector %s.".formatted(selector);
                throw new JUnitException(message);
            }

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
                clazz -> clazz.getSimpleName().equals(name) || clazz.getCanonicalName().equals(name),
                _ -> true);
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private static Method findMethod(Class<?> clazz, String name) {
        List<Method> matches = Stream.of(clazz.getDeclaredMethods())
            .filter(method -> method.getName().equals(name))
            .toList();
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private static String getParameters(Method method) {
        return Stream.of(method.getParameterTypes())
            .map(Class::getName)
            .collect(Collectors.joining(","));
    }
}
