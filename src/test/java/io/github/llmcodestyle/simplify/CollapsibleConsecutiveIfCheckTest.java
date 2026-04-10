package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CollapsibleConsecutiveIfCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 10;

    @Test
    void consecutiveIfsWithIdenticalTerminatingBodiesProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/CollapsibleConsecutiveIfInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/CollapsibleConsecutiveIfValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(CollapsibleConsecutiveIfCheck.class, resource, NO_PROPS);
    }
}
