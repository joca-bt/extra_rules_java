package extrarulesjava.testrunner;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static javax.xml.transform.OutputKeys.INDENT;

class Reports {
    private static final Comparator<Element> TESTCASE_COMPARATOR = Comparator.comparing(testcase -> testcase.getAttribute("name"));

    public static void generateReport(Path report) {
        Document junitReport = Documents.readDocument(report.getParent().resolve("TEST-junit-jupiter.xml"));
        Document bazelReport = generateReport(collectTests(junitReport));
        Documents.writeDocument(report, bazelReport);
    }

    private static Map<String, Set<Element>> collectTests(Document document) {
        Map<String, Set<Element>> tests = new TreeMap<>();

        NodeList testcases = document.getElementsByTagName("testcase");

        for (int i = 0, size = testcases.getLength(); i < size; i++) {
            Element testcase = (Element) testcases.item(i);
            testcase.setAttribute("name", getTestcaseMethod(testcase));

            tests.computeIfAbsent(getTestcaseClass(testcase), _ -> new TreeSet<>(TESTCASE_COMPARATOR))
                .add(testcase);
        }

        return tests;
    }

    private static Document generateReport(Map<String, Set<Element>> tests) {
        Document document = Documents.newDocument();

        Element testsuites = document.createElement("testsuites");
        document.appendChild(testsuites);

        for (var entry : tests.entrySet()) {
            Element testsuite = document.createElement("testsuite");
            testsuite.setAttribute("name", entry.getKey());
            testsuite.setAttribute("hostname", "");
            testsuite.setAttribute("timestamp", "");
            testsuites.appendChild(testsuite);

            for (var testcase : entry.getValue()) {
                testsuite.appendChild(document.adoptNode(testcase));
            }
        }

        return document;
    }

    private static String getTestcaseClass(Element testcase) {
        return testcase.getAttribute("classname").replaceFirst(".+\\.", "");
    }

    private static String getTestcaseMethod(Element testcase) {
        String method = testcase.getAttribute("name").replaceFirst("\\(.+", "");

        Element systemOut = (Element) testcase.getElementsByTagName("system-out").item(0);
        String displayName = systemOut.getTextContent().trim().split("\n")[1].substring(14);

        String[] components = displayName.split(" > ");
        String last = components[components.length - 1];

        // Parameterized method?
        if (last.startsWith("[")) {
            method = "%s %s".formatted(method, last);
        }

        return method;
    }

    private static class Documents {
        public static Document newDocument() {
            try {
                return DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }

        public static Document readDocument(Path file) {
            try {
                return DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(file.toFile());
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }

        public static void writeDocument(Path file, Document document) {
            try {
                Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
                transformer.setOutputProperty(INDENT, "yes");
                transformer.transform(new DOMSource(document), new StreamResult(file.toFile()));
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
