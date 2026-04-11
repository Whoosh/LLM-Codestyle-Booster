package io.github.llmcodestyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.github.llmcodestyle.forbidden.ForbidAssertKeywordCheck;
import io.github.llmcodestyle.layout.BlankLineAfterCommentCheck;
import io.github.llmcodestyle.layout.UnnecessaryLineWrapCheck;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static java.lang.Integer.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that the full checkstyle.xml configuration is internally consistent.
 *
 * <p>Two categories of tests:
 * <ol>
 *   <li><b>Golden file idempotency</b> — properly-fixed code produces 0 violations
 *       when ALL rules run simultaneously. This proves that fixing rule A's violation
 *       does not create a new violation for rule B.</li>
 *   <li><b>Specific conflict pair tests</b> — targeted tests for known interaction
 *       points between rules.</li>
 * </ol>
 */
class CheckstyleConfigConsistencyTest {

    private static final Path CHECKSTYLE_XML = resolveConfig();
    private static final String ANNOTATION_CHECK = "com.puppycrawl.tools.checkstyle.checks.annotation.AnnotationLocationCheck";
    private static final String LEFT_CURLY_CHECK = "com.puppycrawl.tools.checkstyle.checks.blocks.LeftCurlyCheck";
    private static final String NEED_BRACES_CHECK = "com.puppycrawl.tools.checkstyle.checks.blocks.NeedBracesCheck";
    private static final String COMMENTS_INDENT_CHECK = "com.puppycrawl.tools.checkstyle.checks.indentation.CommentsIndentationCheck";
    private static final String WHITESPACE_AROUND_CHECK = "com.puppycrawl.tools.checkstyle.checks.whitespace.WhitespaceAroundCheck";
    private static final String NO_WS_AFTER_CHECK = "com.puppycrawl.tools.checkstyle.checks.whitespace.NoWhitespaceAfterCheck";
    /**
     * WhitespaceAround default tokens minus ARRAY_INIT — kept in sync with checkstyle.xml.
     */
    private static final String WS_AROUND_TOKENS_NO_ARRAY_INIT =
        "ASSIGN, BAND, BAND_ASSIGN, BOR, BOR_ASSIGN, BSR, BSR_ASSIGN, BXOR, BXOR_ASSIGN, "
            + "COLON, DIV, DIV_ASSIGN, DO_WHILE, EQUAL, GE, GT, LAMBDA, LAND, LCURLY, LE, "
            + "LITERAL_CATCH, LITERAL_DO, LITERAL_ELSE, LITERAL_FINALLY, LITERAL_FOR, LITERAL_IF, "
            + "LITERAL_RETURN, LITERAL_SWITCH, LITERAL_SYNCHRONIZED, LITERAL_TRY, LITERAL_WHILE, "
            + "LOR, LT, MINUS, MINUS_ASSIGN, MOD, MOD_ASSIGN, NOT_EQUAL, PLUS, PLUS_ASSIGN, "
            + "QUESTION, RCURLY, SL, SLIST, SL_ASSIGN, SR, SR_ASSIGN, STAR, STAR_ASSIGN, "
            + "LITERAL_ASSERT, TYPE_EXTENSION_AND";

    @TempDir
    Path tempDir;

    @Test
    void goldenMainFileProducesZeroViolations() throws Exception {
        List<AuditEvent> violations = runFullConfig("valid/IdempotencyGoldenMain.java", false);
        assertTrue(violations.isEmpty(), "Golden main file should have 0 violations but got " + violations.size() + ": " + format(violations));
    }

    @Test
    void goldenTestFileProducesZeroViolations() throws Exception {
        List<AuditEvent> violations = runFullConfig("valid/IdempotencyGoldenTest.java", true);
        assertTrue(violations.isEmpty(), "Golden test file should have 0 violations but got " + violations.size() + ": " + format(violations));
    }

    @Test
    void annotationOnSeparateLineDoesNotTriggerUnnecessaryWrap() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                ANNOTATION_CHECK,
                Map.of("allowSamelineSingleParameterlessAnnotation", "false", "allowSamelineParameterizedAnnotation", "false"),
                UnnecessaryLineWrapCheck.class.getName(),
                Map.of("maxLineLength", "180")),
            "valid/IdempotencyGoldenMain.java");
        long wrapViolations = violations.stream()
            .filter(e -> e.getMessage().contains("Unnecessary line wrap"))
            .filter(CheckstyleConfigConsistencyTest::isNearAnnotation)
            .count();
        assertEquals(0, wrapViolations, "AnnotationLocation fix must not trigger UnnecessaryLineWrap (found " + wrapViolations + "): " + format(violations));
    }

    @Test
    void tempDirOnSeparateLineDoesNotTriggerUnnecessaryWrap() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                ANNOTATION_CHECK,
                Map.of("allowSamelineSingleParameterlessAnnotation", "false", "tokens", "VARIABLE_DEF"),
                UnnecessaryLineWrapCheck.class.getName(),
                Map.of("maxLineLength", "180")),
            "valid/IdempotencyGoldenTest.java");
        long wrapViolations = violations.stream().filter(e -> e.getMessage().contains("Unnecessary line wrap")).count();
        assertEquals(0, wrapViolations, "@TempDir on separate line must not trigger UnnecessaryLineWrap (found " + wrapViolations + "): " + format(violations));
    }

    @Test
    void assertReplacementWithIfThrowSatisfiesLeftCurly() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                LEFT_CURLY_CHECK,
                Map.of(),
                NEED_BRACES_CHECK,
                Map.of(),
                ForbidAssertKeywordCheck.class.getName(),
                Map.of()),
            "valid/IdempotencyGoldenMain.java");
        assertEquals(0, violations.size(), "if/throw replacement for assert must satisfy LeftCurly + NeedBraces: " + format(violations));
    }

    @Test
    void commentDirectlyAboveCodeSatisfiesBothCommentChecks() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(Map.of(COMMENTS_INDENT_CHECK, Map.of()), "valid/IdempotencyGoldenMain.java");
        List<AuditEvent> blankLineViolations = runFileSetCheck(BlankLineAfterCommentCheck.class, "valid/IdempotencyGoldenMain.java", Map.of("fileExtensions", "java"));
        assertEquals(
            0,
            violations.size() + blankLineViolations.size(),
            "Comment placement must satisfy CommentsIndentation and BlankLineAfterComment: " + format(violations) + format(blankLineViolations));
    }

    @Test
    void chainedCallBrokenAcrossLinesDoesNotTriggerUnnecessaryWrap() throws Exception {
        List<AuditEvent> violations = runChainPlusWrap("valid/IdempotencyGoldenMain.java");
        assertEquals(
            0,
            violations.stream()
                .filter(e -> e.getMessage().contains("Unnecessary line wrap"))
                .filter(e -> e.getLine() >= findChainedResultLine())
                .count(),
            "4+ chain broken across lines must not trigger UnnecessaryLineWrap: " + format(violations));
    }

    @Test
    void multipleChainTypesDoNotTriggerUnnecessaryWrap() throws Exception {
        List<AuditEvent> violations = runChainPlusWrap("valid/IdempotencyGoldenMain.java");
        long wrapViolations = countMessages(violations, "Unnecessary line wrap");
        long chainViolations = countMessages(violations, "Chained method calls");
        assertEquals(0, wrapViolations, "No chain should trigger UnnecessaryLineWrap: " + format(violations));
        assertEquals(0, chainViolations, "No chain should trigger ChainedCallLineBreak: " + format(violations));
    }

    @Test
    void shortChainOnOneLineDoesNotTriggerChainedCallBreak() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of("io.github.llmcodestyle.layout.ChainedCallLineBreakCheck", Map.of("minChainLength", "4")),
            "valid/IdempotencyGoldenMain.java");
        assertEquals(0, countMessages(violations, "Chained method calls"), "3-call chain on one line must not trigger ChainedCallLineBreak: " + format(violations));
    }

    @Test
    void methodCallArgsAndUnnecessaryWrapDoNotConflict() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                "io.github.llmcodestyle.layout.MethodCallArgumentsOnSameLineCheck",
                Map.of(),
                UnnecessaryLineWrapCheck.class.getName(),
                Map.of("maxLineLength", "180")),
            "valid/IdempotencyGoldenMain.java");
        assertEquals(0, violations.size(), "MethodCallArguments and UnnecessaryLineWrap must not conflict: " + format(violations));
    }

    @Test
    void singleUseVarAndChainedCallDoNotConflict() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                "io.github.llmcodestyle.simplify.SingleUseLocalVariableCheck",
                Map.of(),
                "io.github.llmcodestyle.layout.ChainedCallLineBreakCheck",
                Map.of("minChainLength", "4")),
            "valid/IdempotencyGoldenMain.java");
        assertEquals(0, violations.size(), "SingleUseLocalVariable and ChainedCallLineBreak must not conflict: " + format(violations));
    }

    @Test
    void compactableParamsAndMethodCallArgsDoNotConflict() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                "io.github.llmcodestyle.layout.CompactableParameterListCheck",
                Map.of("maxLineLength", "180"),
                "io.github.llmcodestyle.layout.MethodCallArgumentsOnSameLineCheck",
                Map.of()),
            "valid/IdempotencyGoldenMain.java");
        assertEquals(0, violations.size(), "CompactableParameterList and MethodCallArguments must not conflict: " + format(violations));
    }

    @Test
    void arrayInitWhitespaceChecksDoNotPingPong() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                WHITESPACE_AROUND_CHECK,
                Map.of("tokens", WS_AROUND_TOKENS_NO_ARRAY_INIT),
                NO_WS_AFTER_CHECK,
                Map.of()),
            "valid/IdempotencyGoldenMain.java");
        assertEquals(0, violations.size(), "WhitespaceAround (ARRAY_INIT excluded) and NoWhitespaceAfter must not conflict on array init: " + format(violations));
    }

    @Test
    void stressTestChainPatternsProduceNoLayoutConflicts() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                UnnecessaryLineWrapCheck.class.getName(),
                Map.of("maxLineLength", "180"),
                "io.github.llmcodestyle.layout.ChainedCallLineBreakCheck",
                Map.of("minChainLength", "4"),
                "io.github.llmcodestyle.layout.MethodCallArgumentsOnSameLineCheck",
                Map.of()),
            "valid/ChainStressTest.java");
        assertEquals(0, countMessages(violations, "Unnecessary line wrap"), "Stress test: no UnnecessaryLineWrap violations expected");
        assertEquals(0, countMessages(violations, "Chained method calls"), "Stress test: no ChainedCallLineBreak violations expected");
        assertEquals(0, countMessages(violations, "mixed line layout"), "Stress test: no MethodCallArguments violations expected");
    }

    private static List<AuditEvent> runChainPlusWrap(String resource) throws Exception {
        return runMultipleTreeWalkerChecks(
            Map.of(
                UnnecessaryLineWrapCheck.class.getName(),
                Map.of("maxLineLength", "180"),
                "io.github.llmcodestyle.layout.ChainedCallLineBreakCheck",
                Map.of("minChainLength", "4")),
            resource);
    }

    private static long countMessages(List<AuditEvent> violations, String marker) {
        return violations.stream().filter(e -> e.getMessage().contains(marker)).count();
    }

    private static int findChainedResultLine() {
        try {
            URL resource = CheckstyleConfigConsistencyTest.class.getClassLoader().getResource("valid/IdempotencyGoldenMain.java");
            if (resource == null) {
                return MAX_VALUE;
            }
            List<String> lines = Files.readAllLines(Path.of(resource.toURI()));
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains("chainedResult")) {
                    return i + 1;
                }
            }
        } catch (IllegalArgumentException | java.io.IOException | java.net.URISyntaxException ignored) {
            // fall through
        }
        return MAX_VALUE;
    }

    // Custom check registry lives in src/test/resources/custom-checks-registry.txt —
    // CrossAnalyzerConsistencyTest.everyCustomCheckParticipatesToConsistencyTest scans it
    // for class names and fails if any *Check class is missing from the registry.
    private List<AuditEvent> runFullConfig(String resourceFile, boolean testScope) throws Exception {
        if (CHECKSTYLE_XML == null || !Files.exists(CHECKSTYLE_XML)) {
            return List.of();
        }
        URL resource = getClass().getClassLoader().getResource(resourceFile);
        if (resource == null) {
            throw new IllegalStateException("Resource not found: " + resourceFile);
        }
        Path targetDir = tempDir.resolve(testScope ? "src/test/java/golden" : "src/main/java/golden");
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(Path.of(resourceFile).getFileName().toString());
        Files.copy(Path.of(resource.toURI()), targetFile);
        Configuration config = ConfigurationLoader.loadConfiguration(CHECKSTYLE_XML.toAbsolutePath().toString(), new PropertiesExpander(new Properties()));
        Checker checker = new Checker();
        checker.setModuleClassLoader(getClass().getClassLoader());
        checker.configure(config);
        List<AuditEvent> violations = new ArrayList<>();
        checker.addListener(new TestAuditListener(violations));
        checker.process(List.of(targetFile.toFile()));
        checker.destroy();
        return violations;
    }

    private static boolean isNearAnnotation(AuditEvent event) {
        try {
            URL resource = CheckstyleConfigConsistencyTest.class.getClassLoader().getResource("valid/IdempotencyGoldenMain.java");
            if (resource == null) {
                return false;
            }
            List<String> lines = Files.readAllLines(Path.of(resource.toURI()));
            int lineIdx = event.getLine() - 1;
            if (lineIdx > 0 && lineIdx < lines.size()) {
                return lines.get(lineIdx - 1).strip().startsWith("@");
            }
        } catch (IllegalArgumentException | java.io.IOException | java.net.URISyntaxException ignored) {
            // fall through
        }
        return false;
    }

    private static Path resolveConfig() {
        Path fromModule = Path.of("../checkstyle.xml");
        if (Files.exists(fromModule)) {
            return fromModule;
        }
        Path fromRoot = Path.of("checkstyle.xml");
        if (Files.exists(fromRoot)) {
            return fromRoot;
        }
        return null;
    }

}
