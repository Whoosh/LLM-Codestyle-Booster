package io.github.llmcodestyle.forbidden;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NoSuppressionCheckTest {

    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupportUtil.runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/invalid/NoSuppressionInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 4 suppression violations (2 comments + 2 annotations), got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(TestCheckSupportUtil.runTreeWalkerCheck(NoSuppressionCheck.class, "forbidden/valid/NoSuppressionValid.java", Map.of()).isEmpty(), "Expected no violations");
    }
}
