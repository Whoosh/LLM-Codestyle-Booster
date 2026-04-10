package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
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

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(ConditionalReturnToTernaryCheck.class, resource, NO_PROPS);
    }
}
