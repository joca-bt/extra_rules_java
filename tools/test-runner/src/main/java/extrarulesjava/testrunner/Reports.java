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
    private static final Comparator<Element> ELEMENT_COMPARATOR = Comparator.comparing(element -> element.getAttribute("name"));

    public static void generateReport(Path report) {
        Document junitReport = Documents.readDocument(report.getParent().resolve("TEST-junit-jupiter.xml"));
        Document bazelReport = generateReport(collectTests(junitReport));
        Documents.writeDocument(bazelReport, report);
    }

    /**
     * Adjusts the name of a testcase:
     *   - Removes parameters.
     *   - Adds parameterization details, if applicable.
     */
    private static void adjustTestcaseName(Element testcase) {
        String name = testcase.getAttribute("name").replaceAll("\\(.+", "");
        String parameterizationDetails = testcase.getTextContent().replaceAll("(?s).+display-name: (\\V+).+", "$1");

        // Parameterized test?
        if (parameterizationDetails.startsWith("[")) {
            name = "%s %s".formatted(name, parameterizationDetails);
        }

        testcase.setAttribute("name", name);
    }

    private static Map<String, Set<Element>> collectTests(Document document) {
        Map<String, Set<Element>> tests = new TreeMap<>();

        NodeList testcases = document.getElementsByTagName("testcase");

        for (int i = 0, size = testcases.getLength(); i < size; i++) {
            Element testcase = (Element) testcases.item(i);
            adjustTestcaseName(testcase);

            String group = testcase.getAttribute("classname").replaceAll(".+\\.", "");

            tests.computeIfAbsent(group, __ -> new TreeSet<>(ELEMENT_COMPARATOR))
                .add(testcase);
        }

        return tests;
    }

    private static Document generateReport(Map<String, Set<Element>> tests) {
        Document document = Documents.newDocument();

        Element testsuites = document.createElement("testsuites");
        document.appendChild(testsuites);

        for (var group : tests.entrySet()) {
            Element testsuite = document.createElement("testsuite");
            testsuites.appendChild(testsuite);

            testsuite.setAttribute("name", group.getKey());
            testsuite.setAttribute("hostname", "");
            testsuite.setAttribute("timestamp", "");

            for (var test : group.getValue()) {
                testsuite.appendChild(document.adoptNode(test));
            }
        }

        return document;
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

        public static void writeDocument(Document document, Path file) {
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
