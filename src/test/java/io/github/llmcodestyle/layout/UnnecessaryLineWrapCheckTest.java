package io.github.llmcodestyle.layout;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnnecessaryLineWrapCheckTest {

    private static final int EXPECTED_VIOLATIONS = 13;
    private static final String SHORT_MAX_LINE = "100";

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            UnnecessaryLineWrapCheck.class,
            "invalid/UnnecessaryLineWrapInvalid.java",
            Map.of("maxLineLength", "180"));
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 13 unnecessary wrap violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            TestCheckSupport.runTreeWalkerCheck(
                UnnecessaryLineWrapCheck.class,
                "valid/UnnecessaryLineWrapValid.java",
                Map.of("maxLineLength", "180")).isEmpty(),
            "Expected no violations");
    }

    @Test
    void setMaxLineLengthAffectsThreshold() throws Exception {
        new UnnecessaryLineWrapCheck().setMaxLineLength(Integer.parseInt(SHORT_MAX_LINE));
        assertFalse(
            TestCheckSupport.runTreeWalkerCheck(
                UnnecessaryLineWrapCheck.class,
                "invalid/UnnecessaryLineWrapInvalid.java",
                Map.of("maxLineLength", SHORT_MAX_LINE)).isEmpty(),
            "Smaller max line length should produce violations");
    }
}
