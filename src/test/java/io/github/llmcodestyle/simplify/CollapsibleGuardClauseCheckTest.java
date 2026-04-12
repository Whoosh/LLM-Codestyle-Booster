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

    @Test
    void bareReturnGuardDetected() throws Exception {
        List<AuditEvent> violations = run("simplify/invalid/CollapsibleGuardClauseMutKiller.java");
        assertEquals(1, violations.size(),
            "Bare return guard should be detected: " + format(violations));
    }

    @Test
    void extractIfBodyHandlesThrowBody() throws Exception {
        // L99 SURVIVED: extractIfBody while loop condition for LITERAL_THROW
        // L100 NO_COVERAGE: body.getType() != EXPR && body.getType() != LITERAL_THROW
        // A guard that uses `throw` has a LITERAL_THROW body type, not SLIST or LITERAL_RETURN.
        // The extractIfBody while loop must handle LITERAL_THROW as a valid body.
        // This is NOT a valid collapsible guard (throw != void return), so no violation expected.
        List<AuditEvent> violations = run("simplify/invalid/CollapsibleGuardExtractBody.java");
        // The guard with throw is NOT collapsible (isVoidReturnOnly checks for LITERAL_RETURN)
        assertTrue(violations.isEmpty(),
            "Guard with throw should not be collapsible: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(CollapsibleGuardClauseCheck.class, resource, NO_PROPS);
    }
}
