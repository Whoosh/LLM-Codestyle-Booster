package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class BlankLineAfterCommentCheckTest {

    private static final int EXPECTED_VIOLATIONS = 5;
    private static final Map<String, String> JAVA_EXT = Map.of("fileExtensions", "java");

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(BlankLineAfterCommentCheck.class, "layout/invalid/BlankLineAfterCommentInvalid.java", JAVA_EXT);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            runFileSetCheck(BlankLineAfterCommentCheck.class, "layout/valid/BlankLineAfterCommentValid.java", JAVA_EXT).isEmpty(),
            "Expected no violations");
    }

    @Test
    void messageIsDescriptive() throws Exception {
        for (AuditEvent event : runFileSetCheck(BlankLineAfterCommentCheck.class, "layout/invalid/BlankLineAfterCommentInvalid.java", JAVA_EXT)) {
            assertTrue(event.getMessage().contains("blank line"), "Expected 'blank line' in message, got: " + event.getMessage());
        }
    }

    @Test
    void edgeCaseValidFixtureProducesNoViolations() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(BlankLineAfterCommentCheck.class,
            "layout/valid/BlankLineAfterCommentEdgeCases.java", JAVA_EXT);
        assertTrue(violations.isEmpty(), "Edge case valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void violationLinesPointToCommentNotBlankLine() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(BlankLineAfterCommentCheck.class,
            "layout/invalid/BlankLineAfterCommentInvalid.java", JAVA_EXT);
        for (AuditEvent event : violations) {
            assertTrue(event.getLine() > 0, "Line number should be positive: " + event.getLine());
        }
    }

    @Test
    void multiLineBlockCommentWithBlankAfterIsFlagged() throws Exception {
        List<AuditEvent> violations = runFileSetCheck(BlankLineAfterCommentCheck.class,
            "layout/invalid/BlankLineAfterCommentMultiBlock.java", JAVA_EXT);
        assertEquals(1, violations.size(),
            "Expected exactly 1 violation for multi-line block comment with blank after: " + format(violations));
    }

    @Test
    void blockCommentBodyWithTrailingContentNotFlagged() throws Exception {
        // handleBlockCommentBody: */ with trailing content should flushAndReset, not mark as comment
        List<AuditEvent> violations = runFileSetCheck(BlankLineAfterCommentCheck.class,
            "layout/valid/BlankLineAfterCommentBlockBody.java", JAVA_EXT);
        assertTrue(violations.isEmpty(),
            "Block comment body with trailing content should not produce violations: " + format(violations));
    }

    @Test
    void handleBlockCommentBodyClosingLineEmpty() throws Exception {
        // L50 SURVIVED: `stripped.substring(stripped.indexOf("*/") + 2).strip().isEmpty()`
        // Multi-line block comment ending with `*/` on its own line (nothing after */)
        // should mark as comment. If negated, this line would NOT be marked, missing the violation.
        List<AuditEvent> violations = runFileSetCheck(BlankLineAfterCommentCheck.class,
            "layout/invalid/BlankLineAfterBlockCommentEnd.java", JAVA_EXT);
        assertTrue(violations.size() >= 1,
            "Block comment with empty closing line followed by blank should be flagged: " + format(violations));
    }

    @Test
    void blockCommentWithCodeAfterClosingNotFlagged() throws Exception {
        // When `*/` is followed by code on the same line, it's NOT a pure comment line.
        // If L50 is negated, this would be treated as comment, causing false positive.
        List<AuditEvent> violations = runFileSetCheck(BlankLineAfterCommentCheck.class,
            "layout/valid/BlankLineAfterBlockCommentWithCode.java", JAVA_EXT);
        assertTrue(violations.isEmpty(),
            "Block comment with code after */ should not produce violations: " + format(violations));
    }
}
