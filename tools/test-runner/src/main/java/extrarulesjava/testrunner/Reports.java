package extrarulesjava.testrunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.platform.commons.JUnitException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static javax.xml.transform.OutputKeys.INDENT;

class Reports {
    public static void generateReport(Path report) {
        try {
            Document document = readReports(report.getParent());
            writeReport(document, report);
        } catch (Exception exception) {
            String message = "Could not generate report.";
            throw new JUnitException(message, exception);
        }
    }

    /**
     * Fixes the name of a testcase:
     *   - Appends the display name for parameterized tests.
     *   - Removes parameters, since they break IntelliJ IDEA's navigation.
     */
    private static void fixTestcaseName(Element testcase) {
        String name = testcase.getAttribute("name");
        String displayName = getTestcaseDisplayName(testcase);

        name = name.substring(0, name.indexOf('('));

        // Parameterized test?
        if (displayName.startsWith("[")) {
            name = "%s %s".formatted(name, displayName);
        }

        testcase.setAttribute("name", name);
    }

    /*
     * <system-out><![CDATA[
     * unique-id: ...
     * display-name: ...
     * ]]></system-out>
     */
    private static String getTestcaseDisplayName(Element testcase) {
        Element systemOut = (Element) testcase.getElementsByTagName("system-out").item(0);
        String[] properties = systemOut.getTextContent().trim().split("\n");
        return properties[1].substring(14);
    }

    private static Document readReports(Path dir) throws Exception {
        List<Path> files;

        try (var stream = Files.list(dir)) {
            files = stream
                .filter(file -> file.getFileName().toString().matches("TEST-junit-.+\\.xml"))
                .toList();
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("testsuites");

        document.appendChild(root);

        for (var file : files) {
            Document subdocument = builder.parse(file.toFile());
            NodeList testsuites = subdocument.getElementsByTagName("testsuite");

            for (int i = 0, sizei = testsuites.getLength(); i < sizei; i++) {
                Element testsuite = (Element) testsuites.item(i);
                NodeList testcases = testsuite.getElementsByTagName("testcase");

                for (int j = 0, sizej = testcases.getLength(); j < sizej; j++) {
                    Element testcase = (Element) testcases.item(j);
                    fixTestcaseName(testcase);
                }

                root.appendChild(document.adoptNode(testsuite));
            }
        }

        return document;
    }

    private static void writeReport(Document document, Path file) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(file.toFile());

        transformer.setOutputProperty(INDENT, "yes");
        transformer.transform(source, result);
    }
}
