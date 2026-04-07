package io.github.llmcodestyle.layout;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompactableParameterListCheckTest {

    private static final int EXPECTED_VIOLATIONS = 5;
    private static final String SHORT_MAX_LINE = "80";

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            CompactableParameterListCheck.class,
            "layout/invalid/CompactableParamInvalid.java",
            Map.of("maxLineLength", "180"));
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 compactable parameter violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            TestCheckSupport.runTreeWalkerCheck(
                CompactableParameterListCheck.class,
                "layout/valid/CompactableParamValid.java",
                Map.of("maxLineLength", "180")).isEmpty(),
            "Expected no violations");
    }

    @Test
    void setMaxLineLengthAffectsThreshold() throws Exception {
        new CompactableParameterListCheck().setMaxLineLength(Integer.parseInt(SHORT_MAX_LINE));
        assertFalse(
            TestCheckSupport.runTreeWalkerCheck(
                CompactableParameterListCheck.class,
                "layout/invalid/CompactableParamInvalid.java",
                Map.of("maxLineLength", SHORT_MAX_LINE)).isEmpty(),
            "Smaller max line length should produce violations");
    }
}
