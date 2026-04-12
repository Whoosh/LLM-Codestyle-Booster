package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class ForbidAssertKeywordCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;
    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int FIRST_ASSERT_LINE = 6;
    private static final int SECOND_ASSERT_LINE = 10;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "forbidden/invalid/ForbidAssertKeywordInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 assert violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "forbidden/valid/ForbidAssertKeywordValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void messageContainsPreconditionsGuidance() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "forbidden/invalid/ForbidAssertKeywordInvalid.java", NO_PROPS)) {
            assertTrue(event.getMessage().contains("Preconditions"), "Expected Preconditions mention in message, got: " + event.getMessage());
        }
    }

    @Test
    void violationsReportCorrectLineNumbers() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ForbidAssertKeywordCheck.class, "forbidden/invalid/ForbidAssertKeywordInvalid.java", NO_PROPS);
        assertEquals(2, violations.size());
        assertTrue(violations.get(0).getLine() == FIRST_ASSERT_LINE, "First assert should be on line 6: " + violations.get(0).getLine());
        assertTrue(violations.get(1).getLine() == SECOND_ASSERT_LINE, "Second assert should be on line 10: " + violations.get(1).getLine());
    }

    @Test
    void tokenArraysReturnLiteralAssert() {
        ForbidAssertKeywordCheck check = new ForbidAssertKeywordCheck();
        int[] defaultTokens = check.getDefaultTokens();
        int[] acceptableTokens = check.getAcceptableTokens();
        int[] requiredTokens = check.getRequiredTokens();
        assertEquals(1, defaultTokens.length, "Should have exactly 1 token");
        assertEquals(1, acceptableTokens.length, "Should have exactly 1 acceptable token");
        assertEquals(1, requiredTokens.length, "Should have exactly 1 required token");
        assertEquals(defaultTokens[0], acceptableTokens[0], "Default and acceptable should match");
        assertEquals(defaultTokens[0], requiredTokens[0], "Default and required should match");
    }
}
