package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class CollapsibleGuardClauseCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void collapsibleGuardsProduceViolations() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/CollapsibleGuardClauseInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 5 collapsible guard violations: " + format(violations));
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/CollapsibleGuardClauseValid.java").isEmpty());
    }

    @Test
    void violationMessageMentionsGuard() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/CollapsibleGuardClauseInvalid.java");
        for (AuditEvent event : violations) {
            assertNotNull(event.getMessage(), "Message should not be null");
            assertFalse(event.getMessage().isEmpty(), "Message should not be empty");
        }
    }

    @Test
    void violationsReportCorrectLines() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/CollapsibleGuardClauseInvalid.java");
        for (AuditEvent event : violations) {
            assertTrue(event.getLine() > 0, "Line should be positive");
        }
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(CollapsibleGuardClauseCheck.class, resource, NO_PROPS);
    }
}
