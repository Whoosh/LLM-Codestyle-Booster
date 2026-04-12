package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class IndexOfToContainsCheckTest {

    private static final int EXPECTED_VIOLATIONS = 9;

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(IndexOfToContainsCheck.class, "simplify/invalid/IndexOfToContainsInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 9 indexOf-vs-contains violations (5 normal + 4 reversed), got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(IndexOfToContainsCheck.class, "simplify/valid/IndexOfToContainsValid.java", Map.of()).isEmpty(), "Expected no violations");
    }

    @Test
    void charLiteralAndMethodCallArgNotFlagged() throws Exception {
        // indexOf with char literal or method call arg should not be flagged
        // This exercises isValidIndexOfArgument lines 83, 87, 90
        assertTrue(
            runTreeWalkerCheck(IndexOfToContainsCheck.class, "simplify/valid/IndexOfToContainsValid.java", Map.of()).isEmpty(),
            "char literal / method call indexOf args should be valid");
    }

    @Test
    void violationsAreExactCount() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(IndexOfToContainsCheck.class, "simplify/invalid/IndexOfToContainsInvalid.java", Map.of());
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Exact violation count: " + format(violations));
        for (AuditEvent v : violations) {
            assertTrue(v.getLine() > 0, "Line should be positive");
            assertFalse(v.getMessage().isEmpty(), "Message should not be empty");
        }
    }
}
