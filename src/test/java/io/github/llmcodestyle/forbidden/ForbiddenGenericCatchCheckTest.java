package io.github.llmcodestyle.forbidden;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ForbiddenGenericCatchCheckTest {

    private static final int EXPECTED_VIOLATIONS = 6;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupportUtil.runTreeWalkerCheck(ForbiddenGenericCatchCheck.class, "forbidden/invalid/ForbiddenGenericCatchInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 6 generic catch violations, got: " + violations.size());
        for (AuditEvent event : violations) {
            assertTrue(event.getMessage().contains("forbidden"), "Unexpected message: " + event.getMessage());
        }
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            TestCheckSupportUtil.runTreeWalkerCheck(
                ForbiddenGenericCatchCheck.class, "forbidden/valid/ForbiddenGenericCatchValid.java", Map.of()).isEmpty(),
            "Expected no violations");
    }
}
