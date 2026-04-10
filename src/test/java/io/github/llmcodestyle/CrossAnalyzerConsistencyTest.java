package io.github.llmcodestyle;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

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
    void everySimplifyCheckCoveredByAsymptoticSafetyTest() throws Exception {
        Path safetyTest = Path.of("src/test/java/io/github/llmcodestyle/AsymptoticSafetyTest.java");
        assumeTrue(Files.exists(safetyTest), "AsymptoticSafetyTest.java not found");
        String safetyTestContent = Files.readString(safetyTest);
        List<String> simplifyChecks = findCheckClassNamesInSubpackage();
        assertFalse(simplifyChecks.isEmpty(), "No checks found in simplify subpackage");
        List<String> missing = simplifyChecks.stream().filter(name -> !safetyTestContent.contains(name)).toList();
        assertTrue(missing.isEmpty(), "Simplify checks not referenced in AsymptoticSafetyTest: " + missing);
    }

    @Test
    void everyCustomCheckIsRegisteredInCheckstyleXml() throws Exception {
        String xml = readProjectFile("checkstyle.xml");
        List<String> checkNames = findAllCheckClassNames();
        assertFalse(checkNames.isEmpty(), "No *Check classes found in module");
        List<String> missing = checkNames.stream().filter(name -> !xml.contains(name)).toList();
        assertTrue(missing.isEmpty(), "Custom checks not registered in checkstyle.xml: " + missing);
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

    private static List<String> findCheckClassNamesInSubpackage() throws IOException {
        Path dir = Path.of("src/main/java/io/github/llmcodestyle/simplify");
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(dir)) {
            return files
                .map(p -> p.getFileName().toString())
                .filter(n -> n.endsWith("Check.java"))
                .map(n -> n.replace(".java", ""))
                .sorted()
                .toList();
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
