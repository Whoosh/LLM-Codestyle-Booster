package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class ForbidAssertKeywordCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "forbidden/invalid/ForbidAssertKeywordInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 assert violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "forbidden/valid/ForbidAssertKeywordValid.java", Map.of()).isEmpty(), "Expected no violations");
    }

    @Test
    void messageContainsPreconditionsGuidance() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "forbidden/invalid/ForbidAssertKeywordInvalid.java", Map.of())) {
            assertTrue(event.getMessage().contains("Preconditions"), "Expected Preconditions mention in message, got: " + event.getMessage());
        }
    }
}
