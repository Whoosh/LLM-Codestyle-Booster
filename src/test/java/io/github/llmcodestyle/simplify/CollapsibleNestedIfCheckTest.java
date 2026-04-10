package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CollapsibleNestedIfCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void collapsibleNestedIfsProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/CollapsibleNestedIfInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/CollapsibleNestedIfValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupport.runTreeWalkerCheck(CollapsibleNestedIfCheck.class, resource, NO_PROPS);
    }
}
