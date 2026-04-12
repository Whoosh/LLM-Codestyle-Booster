package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class LongTestLiteralCheckTest {

    private static final int EXPECTED_VIOLATIONS = 3;
    private static final String SHORT_MAX = "10";
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/invalid/LongTestLiteralInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 3 long test literal violations, got " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(LongTestLiteralCheck.class, "quality/valid/LongTestLiteralValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void setMaxLengthAffectsThreshold() throws Exception {
        new LongTestLiteralCheck().setMaxLength(Integer.parseInt(SHORT_MAX));
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class,
            "quality/invalid/LongTestLiteralInvalid.java", Map.of("maxLength", SHORT_MAX));
        assertFalse(violations.isEmpty(), "Smaller max length should produce violations");
    }

    @Test
    void edgeCaseValidFixtureProducesNoViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class,
            "quality/valid/LongTestLiteralEdgeCases.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Edge case valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void fieldConstantsAreExempt() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class,
            "quality/valid/LongTestLiteralValid.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Field constants should be exempt");
    }

    @Test
    void assertionMessagesAreExempt() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class,
            "quality/valid/LongTestLiteralValid.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Assertion messages (last arg) should be exempt");
    }

    @Test
    void displayNameAnnotationIsExempt() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class,
            "quality/valid/LongTestLiteralEdgeCases.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "DisplayName annotation values should be exempt");
    }

    @Test
    void violationMessageContainsLengthInfo() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class,
            "quality/invalid/LongTestLiteralInvalid.java", NO_PROPS);
        for (AuditEvent event : violations) {
            String msg = event.getMessage();
            assertTrue(msg.contains("30") || msg.contains("character"),
                "Message should reference max length: " + msg);
        }
    }

    @Test
    void nonTestMethodLongLiteralsAreExempt() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(LongTestLiteralCheck.class,
            "quality/valid/LongTestLiteralEdgeCases.java", NO_PROPS);
        assertTrue(violations.isEmpty(), "Long literals in non-test methods should be exempt");
    }
}
