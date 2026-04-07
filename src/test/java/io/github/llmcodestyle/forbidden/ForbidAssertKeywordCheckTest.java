package io.github.llmcodestyle.forbidden;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForbidAssertKeywordCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "invalid/ForbidAssertKeywordInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 assert violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "valid/ForbidAssertKeywordValid.java", Map.of()).isEmpty(), "Expected no violations");
    }

    @Test
    void messageContainsPreconditionsGuidance() throws Exception {
        for (AuditEvent event : TestCheckSupport.runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "invalid/ForbidAssertKeywordInvalid.java", Map.of())) {
            assertTrue(event.getMessage().contains("Preconditions"), "Expected Preconditions mention in message, got: " + event.getMessage());
        }
    }
}
