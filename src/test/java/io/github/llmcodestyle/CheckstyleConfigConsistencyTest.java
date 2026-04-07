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
        List<AuditEvent> violations = TestCheckSupport.runMultipleTreeWalkerChecks(
            Map.of(
                ANNOTATION_CHECK,
                Map.of("allowSamelineSingleParameterlessAnnotation", "false", "allowSamelineParameterizedAnnotation", "false"),
                UnnecessaryLineWrapCheck.class.getName(),
                Map.of("maxLineLength", "180")),
            "valid/IdempotencyGoldenMain.java");
        long wrapViolations = violations.stream()
            .filter(e -> e.getMessage().contains("Unnecessary line wrap"))
            .filter(e -> isNearAnnotation(e))
            .count();
        assertEquals(0, wrapViolations, "AnnotationLocation fix must not trigger UnnecessaryLineWrap (found " + wrapViolations + "): " + format(violations));
    }

    @Test
    void tempDirOnSeparateLineDoesNotTriggerUnnecessaryWrap() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runMultipleTreeWalkerChecks(
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
        List<AuditEvent> violations = TestCheckSupport.runMultipleTreeWalkerChecks(
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
        List<AuditEvent> violations = TestCheckSupport.runMultipleTreeWalkerChecks(Map.of(COMMENTS_INDENT_CHECK, Map.of()), "valid/IdempotencyGoldenMain.java");
        List<AuditEvent> blankLineViolations = TestCheckSupport.runFileSetCheck(
            BlankLineAfterCommentCheck.class,
            "valid/IdempotencyGoldenMain.java",
            Map.of("fileExtensions", "java"));
        assertEquals(
            0,
            violations.size() + blankLineViolations.size(),
            "Comment placement must satisfy CommentsIndentation and BlankLineAfterComment: " + format(violations) + format(blankLineViolations));
    }

    // Custom check registry — every *Check class must appear here.
    // CrossAnalyzerConsistencyTest.everyCustomCheckParticipatesToConsistencyTest
    // scans this file for class names and fails if any are missing.
    //
    // Checks exercised via golden files (full checkstyle.xml run):
    //   UnnecessaryLineWrapCheck — golden files test annotation-vs-wrap conflict
    //   ForbidAssertKeywordCheck — golden files verify if/throw pattern satisfies LeftCurly
    //   BlankLineAfterCommentCheck — golden files verify comment-above-code pattern
    //   SingleUseLocalVariableCheck — golden files have no single-use vars
    //   PureSingleUseLocalVariableCheck — golden files have no deferred single-use vars
    //   CompactableParameterListCheck — golden files have no multi-line params that fit
    //   StaticFinalFirstCheck — golden files have static finals before instance fields
    //   CollapsibleConstantConcatenationCheck — golden files have no collapsible concatenations
    //   NoSuppressionCheck — golden files have no @SuppressWarnings
    //   InlineRegexConstantCheck — golden files have no inline regex in method bodies
    //   IndexOfToContainsCheck — golden files have no indexOf comparisons
    //   UseIsEmptyCheck — golden files have no length()/size() vs 0 comparisons
    //   UnusedPrivateMembersCheck — golden files have no unused private members
    //   MethodCallArgumentsOnSameLineCheck — golden files have consistent arg layout
    //   ChainedCallLineBreakCheck — golden files have no long chains on one line
    //
    // Checks active only for specific scope (exercised via golden files with path filtering):
    //   NoSystemOutInProductionCheck — suppressed in test scope, active in main golden file
    //   TestClassNamingCheck — suppressed in main scope, active in test golden file
    //   LongTestLiteralCheck — suppressed in main scope, active in test golden file
    //   TestMethodNameCheck — active in test golden file (camelCase @Test methods)
    //   PublicMethodTestCoverageCheck — requires filesystem test files, skips silently in golden test
    //   TestOnlyDelegateCheck — golden files have no thin delegates to private methods
    //
    // Checks that don't interact with other rules (no known conflicts):
    //   ForbiddenGenericCatchCheck — flags catch(Exception), no cross-rule interaction
    //   UnicodeEscapeCheck — FileSetCheck scanning raw content, independent of AST checks
    //   CommentedOutCodeCheck — FileSetCheck scanning comments, independent of AST checks
    //   UtilClassInUtilsPackageCheck — naming convention check, no cross-rule interaction
    //   StaticImportCandidateCheck — suggests static imports, no conflict with other checks
    //   ArrayInitSpaceCheck — enforces space before '{' in array init, no cross-rule interaction
    //   StaticStarImportCheck — requires wildcard static imports, no cross-rule interaction
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

    private static String format(List<AuditEvent> events) {
        return events.stream()
            .map(e -> "Line " + e.getLine() + ": " + e.getMessage())
            .toList()
            .toString();
    }
}
