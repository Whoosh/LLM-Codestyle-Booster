package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class RedundantConstantAliasCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void aliasesAndDuplicatePatternsAreFlagged() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 violations: " + format(violations));
    }

    @Test
    void uniqueConstantsAndOneOffPatternsProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/RedundantConstantAliasValid.java").isEmpty());
    }

    @Test
    void violationMessageContainsConstantName() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/RedundantConstantAliasInvalid.java");
        for (AuditEvent event : violations) {
            assertNotNull(event.getMessage(), "Message should not be null");
            assertFalse(event.getMessage().isEmpty(), "Message should not be empty");
            assertTrue(event.getLine() > 0, "Line should be positive");
        }
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(RedundantConstantAliasCheck.class, resource, NO_PROPS);
    }
}
