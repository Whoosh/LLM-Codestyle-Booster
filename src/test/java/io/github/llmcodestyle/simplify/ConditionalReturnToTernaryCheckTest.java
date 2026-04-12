package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class ConditionalReturnToTernaryCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 2;

    @Test
    void conditionalReturnProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, runCheck("simplify/invalid/ConditionalReturnToTernaryInvalid.java").size());
    }

    @Test
    void ternaryAndComplexPatternsProduceNoViolations() throws Exception {
        assertTrue(runCheck("simplify/valid/ConditionalReturnToTernaryValid.java").isEmpty());
    }

    @Test
    void violationsHavePositiveLineNumbers() throws Exception {
        List<AuditEvent> violations = runCheck("simplify/invalid/ConditionalReturnToTernaryInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size());
        for (AuditEvent v : violations) {
            assertTrue(v.getLine() > 0, "Line should be positive: " + v.getLine());
            assertFalse(v.getMessage().isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void complexConditionsStayValid() throws Exception {
        // Valid fixture exercises containsType (line 86) and depth (line 96) guards
        // containsType with METHOD_CALL nested inside condition -> returns true -> not flagged
        // depth > 3 in condition -> not flagged
        List<AuditEvent> violations = runCheck("simplify/valid/ConditionalReturnToTernaryValid.java");
        assertTrue(violations.isEmpty(),
            "Complex conditions should not be flagged: " + format(violations));
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(ConditionalReturnToTernaryCheck.class, resource, NO_PROPS);
    }
}
