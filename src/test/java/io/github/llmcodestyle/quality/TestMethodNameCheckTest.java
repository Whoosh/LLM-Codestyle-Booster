package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestMethodNameCheckTest {

    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(TestMethodNameCheck.class, "quality/invalid/TestMethodNameInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 4 underscore violations in test methods, got " + violations.size());
        for (AuditEvent event : violations) {
            assertTrue(event.getMessage().contains("camelCase"), "Expected camelCase mention in message, got: " + event.getMessage());
        }
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runTreeWalkerCheck(TestMethodNameCheck.class, "quality/valid/TestMethodNameValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
