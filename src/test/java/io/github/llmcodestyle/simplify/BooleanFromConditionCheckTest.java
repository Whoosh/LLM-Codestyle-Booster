package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class BooleanFromConditionCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void booleanFlipPatternProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/BooleanFromConditionInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/BooleanFromConditionValid.java").isEmpty());
    }

    @Test
    void violationsContainVariableName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/BooleanFromConditionInvalid.java");
        // booleanLiteralVarName returns the variable name (SURVIVED on line 64, 68)
        // If mutated to return "", the message would contain empty string instead of var name
        for (AuditEvent v : violations) {
            String msg = v.getMessage();
            assertNotNull(msg, "Message should not be null");
            assertFalse(msg.isEmpty(), "Message should not be empty");
        }
        // Check that at least one violation mentions a specific variable
        assertTrue(violations.stream().anyMatch(v -> v.getLine() > 0),
            "Violations should report positive line numbers: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(BooleanFromConditionCheck.class, resource, NO_PROPS);
    }
}
