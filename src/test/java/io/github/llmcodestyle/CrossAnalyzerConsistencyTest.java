package io.github.llmcodestyle;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static javax.xml.XMLConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.w3c.dom.Node.*;

/**
 * Verifies that Checkstyle, PMD, and SpotBugs configurations are consistent.
 *
 * <p>Principle: when rules overlap across tools, our custom Checkstyle checks
 * take priority. Other tools must exclude or raise thresholds for conflicting rules.
 *
 * <h3>Known overlaps (documented here as tests):</h3>
 * <table>
 *   <tr><th>Checkstyle rule</th><th>Overlapping tool rule</th><th>Resolution</th></tr>
 *   <tr><td>SingleUseLocalVariable</td><td>SpotBugs USBR_UNNECESSARY_STORE_BEFORE_RETURN</td>
 *       <td>SpotBugs excluded — Checkstyle is authoritative</td></tr>
 *   <tr><td>StaticImportCandidate</td><td>PMD TooManyStaticImports</td>
 *       <td>PMD threshold raised to 30 — accommodates many static imports</td></tr>
 *   <tr><td>ForbiddenGenericCatch</td><td>PMD AvoidCatchingGenericException</td>
 *       <td>Both active — same fix required, no conflict</td></tr>
 *   <tr><td>NoSystemOutInProduction</td><td>PMD SystemPrintln</td>
 *       <td>Both active — same fix required, no conflict</td></tr>
 *   <tr><td>ForbidAssertKeyword</td><td>—</td><td>No PMD/SpotBugs equivalent</td></tr>
 *   <tr><td>AnnotationLocation (strict)</td><td>—</td><td>No PMD/SpotBugs conflict</td></tr>
 *   <tr><td>UnnecessaryLineWrap</td><td>—</td><td>Style rule, no cross-tool equivalent</td></tr>
 *   <tr><td>BlankLineAfterComment</td><td>—</td><td>Style rule, no cross-tool equivalent</td></tr>
 * </table>
 */
class CrossAnalyzerConsistencyTest {

    private static final Path BUNDLED_CONFIG = Path.of("src/main/resources/io/github/llmcodestyle/config");

    @Test
    void spotbugsExcludesUnnecessaryStoreBeforeReturn() throws Exception {
        assertTrue(
            readProjectFile("spotbugs-exclude.xml").contains("USBR_UNNECESSARY_STORE_BEFORE_RETURN"),
            "SpotBugs must exclude USBR_UNNECESSARY_STORE_BEFORE_RETURN — our SingleUseLocalVariableCheck is authoritative");
    }

    @Test
    void spotbugsExcludesUseVarArgs() throws Exception {
        assertTrue(readProjectFile("spotbugs-exclude.xml").contains("UVA_USE_VAR_ARGS"), "SpotBugs must exclude UVA_USE_VAR_ARGS — handled by PMD UseVarargs");
    }

    @Test
    void pmdExcludesUseExplicitTypes() throws Exception {
        assertTrue(
            readProjectFile("pmd-ruleset.xml").contains("<exclude name=\"UseExplicitTypes\"/>"),
            "PMD must exclude UseExplicitTypes — code uses var inference and SingleUseLocalVariableCheck works with var");
    }

    @Test
    void pmdStaticImportThresholdAccommodatesStaticImportCandidate() throws Exception {
        String xml = readProjectFile("pmd-ruleset.xml");
        assertTrue(xml.contains("TooManyStaticImports"), "PMD must configure TooManyStaticImports");
        assertTrue(
            xml.contains("<property name=\"maximumStaticImports\" value=\"30\"/>"),
            "PMD TooManyStaticImports threshold must be >= 30 — StaticImportCandidateCheck suggests many static imports");
    }

    @Test
    void pmdExcludesCommentDefaultAccessModifier() throws Exception {
        assertTrue(
            readProjectFile("pmd-ruleset.xml").contains("<exclude name=\"CommentDefaultAccessModifier\"/>"),
            "PMD must exclude CommentDefaultAccessModifier — convention allows package-private without comment");
    }

    @Test
    void checkstyleAnnotationLocationIsStrict() throws Exception {
        String xml = readProjectFile("checkstyle.xml");
        assertTrue(xml.contains("allowSamelineSingleParameterlessAnnotation\" value=\"false\""), "AnnotationLocation must forbid same-line parameterless annotations");
        assertTrue(xml.contains("allowSamelineParameterizedAnnotation\" value=\"false\""), "AnnotationLocation must forbid same-line parameterized annotations");
        assertTrue(xml.contains("VARIABLE_DEF"), "AnnotationLocation must include VARIABLE_DEF token for field annotations");
    }

    @Test
    void checkstyleHasForbidAssertKeyword() throws Exception {
        assertTrue(readProjectFile("checkstyle.xml").contains("ForbidAssertKeywordCheck"), "Checkstyle must include ForbidAssertKeywordCheck");
    }

    @Test
    void checkstyleHasBlankLineAfterComment() throws Exception {
        assertTrue(readProjectFile("checkstyle.xml").contains("BlankLineAfterCommentCheck"), "Checkstyle must include BlankLineAfterCommentCheck");
    }

    @Test
    void selfCheckXmlAllowsStaticStarImportsLikeBundledConfig() throws Exception {
        Path selfXml = Path.of("checkstyle-self.xml");
        assumeTrue(Files.exists(selfXml), "checkstyle-self.xml not found");
        assertEquals(
            readProjectFile("checkstyle.xml").contains("allowStaticMemberImports\" value=\"true\""),
            Files.readString(selfXml).contains("allowStaticMemberImports\" value=\"true\""),
            "checkstyle-self.xml and bundled checkstyle.xml must agree on allowStaticMemberImports");
    }

    @Test
    void asymptoticSafetyTestExercisesAllSimplifyChecks() throws Exception {
        Path safetyTest = Path.of("src/test/java/io/github/llmcodestyle/AsymptoticSafetyTest.java");
        assumeTrue(Files.exists(safetyTest), "AsymptoticSafetyTest.java not found");
        String content = Files.readString(safetyTest);
        // The dynamic loop in AsymptoticSafetyTest enumerates the simplify package via reflection;
        // verify the discovery code path is present so this contract is not silently regressed.
        assertTrue(content.contains("io.github.llmcodestyle.simplify"), "AsymptoticSafetyTest must enumerate the simplify package");
        assertTrue(content.contains("assertTimeoutPreemptively"), "AsymptoticSafetyTest must enforce a per-check timeout");
    }

    @Test
    void everyCustomCheckIsRegisteredInCheckstyleXml() throws Exception {
        Set<String> registeredFqns = readRegisteredCustomModuleFqns();
        List<String> classFqns = findAllCheckClassFqns();
        assertFalse(classFqns.isEmpty(), "No *Check classes found in module");

        List<String> missingFromConfig = classFqns.stream()
            .filter(fqn -> !registeredFqns.contains(fqn))
            .sorted()
            .toList();
        assertTrue(missingFromConfig.isEmpty(), "Custom checks not registered in checkstyle.xml: " + missingFromConfig);

        List<String> orphanedInConfig = registeredFqns.stream()
            .filter(fqn -> !classFqns.contains(fqn))
            .sorted()
            .toList();
        assertTrue(orphanedInConfig.isEmpty(), "Modules registered in checkstyle.xml without corresponding *Check class: " + orphanedInConfig);
    }

    @Test
    void everyCustomCheckHasOwnTest() throws Exception {
        List<String> checkNames = findAllCheckClassNames();
        Path testDir = Path.of("src/test/java/io/github/llmcodestyle");
        assumeTrue(Files.isDirectory(testDir), "Test directory not found");
        List<String> missingTests = checkNames.stream().filter(name -> {
            try (Stream<Path> walk = Files.walk(testDir)) {
                return walk.noneMatch(p -> p.getFileName().toString().equals(name + "Test.java"));
            } catch (IOException e) {
                return true;
            }
        }).toList();
        assertTrue(missingTests.isEmpty(), "Custom checks without dedicated *Test class: " + missingTests);
    }

    @Test
    void everyCustomCheckParticipatesToConsistencyTest() throws Exception {
        List<String> checkNames = findAllCheckClassNames();
        String registry = Files.readString(Path.of("src/test/resources/custom-checks-registry.txt"));
        List<String> notCovered = checkNames.stream().filter(name -> !registry.contains(name)).toList();
        assertTrue(notCovered.isEmpty(), "Custom checks missing from custom-checks-registry.txt: " + notCovered);
    }

    private static List<String> findAllCheckClassNames() throws IOException {
        Path checkDir = Path.of("src/main/java/io/github/llmcodestyle");
        if (!Files.isDirectory(checkDir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(checkDir)) {
            return files
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .filter(n -> n.endsWith("Check.java"))
                .map(n -> n.replace(".java", ""))
                .sorted()
                .toList();
        }
    }

    /**
     * Walks the source tree and builds the fully-qualified name (e.g.
     * {@code io.github.llmcodestyle.simplify.IndexOfToContainsCheck}) of every {@code *Check.java}
     * source file under the {@code io.github.llmcodestyle} root.
     */
    private static List<String> findAllCheckClassFqns() throws IOException {
        Path root = Path.of("src/main/java");
        Path checkDir = root.resolve("io/github/llmcodestyle");
        if (!Files.isDirectory(checkDir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(checkDir)) {
            return files
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith("Check.java"))
                .map(p -> {
                    String relative = root.relativize(p).toString().replace(java.io.File.separatorChar, '.');
                    return relative.substring(0, relative.length() - ".java".length());
                })
                .sorted()
                .toList();
        }
    }

    /**
     * Parses {@code checkstyle.xml} as XML (NOT plain-text {@code contains()}) and recursively
     * collects every {@code <module name="..."/>} attribute value whose value is in our
     * {@code io.github.llmcodestyle} package. Comments and {@code <property>} elements are
     * skipped by construction because the DOM only returns matching elements.
     */
    private static Set<String> readRegisteredCustomModuleFqns() throws Exception {
        Path xmlPath = BUNDLED_CONFIG.resolve("checkstyle.xml");
        if (!Files.exists(xmlPath)) {
            xmlPath = Path.of("checkstyle.xml");
        }
        assumeTrue(Files.exists(xmlPath), "checkstyle.xml not found");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Avoid network DTD lookup and XXE.
        factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Resolve the DOCTYPE to an empty source so we never hit the network.
        builder.setEntityResolver((publicId, systemId) -> new org.xml.sax.InputSource(new java.io.StringReader("")));
        Document doc = builder.parse(xmlPath.toFile());

        Set<String> result = new TreeSet<>();
        collectModuleNames(doc.getDocumentElement(), result);
        return result;
    }

    private static void collectModuleNames(Element element, Set<String> sink) {
        if ("module".equals(element.getNodeName())) {
            String name = element.getAttribute("name");
            if (name.startsWith("io.github.llmcodestyle.")) {
                sink.add(name);
            }
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == ELEMENT_NODE) {
                collectModuleNames((Element) child, sink);
            }
        }
    }

    private static String readProjectFile(String name) throws Exception {
        Path bundled = BUNDLED_CONFIG.resolve(name);
        if (Files.exists(bundled)) {
            return Files.readString(bundled);
        }
        Path root = Path.of(name);
        assumeTrue(Files.exists(root), "Skipping: " + name + " not found at " + bundled + " or " + root);
        return Files.readString(root);
    }
}
