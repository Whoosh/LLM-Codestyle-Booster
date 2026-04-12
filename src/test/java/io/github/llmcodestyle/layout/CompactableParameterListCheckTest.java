package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class CompactableParameterListCheckTest {

    private static final int EXPECTED_VIOLATIONS = 5;
    private static final String SHORT_MAX_LINE = "80";
    private static final Map<String, String> DEFAULT_PROPS = Map.of("maxLineLength", "180");

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CompactableParameterListCheck.class, "layout/invalid/CompactableParamInvalid.java", DEFAULT_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 compactable parameter violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            runTreeWalkerCheck(CompactableParameterListCheck.class, "layout/valid/CompactableParamValid.java", DEFAULT_PROPS).isEmpty(),
            "Expected no violations");
    }

    @Test
    void setMaxLineLengthAffectsThreshold() throws Exception {
        new CompactableParameterListCheck().setMaxLineLength(Integer.parseInt(SHORT_MAX_LINE));
        List<AuditEvent> violations = runTreeWalkerCheck(CompactableParameterListCheck.class,
            "layout/invalid/CompactableParamInvalid.java", Map.of("maxLineLength", SHORT_MAX_LINE));
        assertFalse(violations.isEmpty(), "Smaller max line length should still produce some violations");
        assertNotEquals(EXPECTED_VIOLATIONS, violations.size(),
            "Different threshold should change the violation count: " + format(violations));
    }

    @Test
    void violationMessageContainsSpaceInfo() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(CompactableParameterListCheck.class,
            "layout/invalid/CompactableParamInvalid.java", DEFAULT_PROPS);
        for (AuditEvent event : violations) {
            assertNotNull(event.getMessage(), "Message should not be null");
            assertFalse(event.getMessage().isEmpty(), "Message should not be empty");
        }
    }
}
