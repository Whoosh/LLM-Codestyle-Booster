package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CollapsibleGuardClauseCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void collapsibleGuardsProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/CollapsibleGuardClauseInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/CollapsibleGuardClauseValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupport.runTreeWalkerCheck(CollapsibleGuardClauseCheck.class, resource, NO_PROPS);
    }
}
