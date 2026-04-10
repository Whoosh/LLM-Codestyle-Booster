package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrChainToSetContainsCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 10;

    @Test
    void orChainsOfLiteralsProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/OrChainToSetContainsInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/OrChainToSetContainsValid.java").isEmpty());
    }

    @Test
    void thresholdIsConfigurable() throws Exception {
        assertFalse(
            TestCheckSupportUtil.runTreeWalkerCheck(OrChainToSetContainsCheck.class, "simplify/valid/OrChainToSetContainsValid.java", Map.of("minOperands", "2")).isEmpty(),
            "minOperands=2 should flag 2-operand chains that are normally skipped");
    }

    @Test
    void setterIsDirectlyInvokable() {
        OrChainToSetContainsCheck check = new OrChainToSetContainsCheck();
        check.setMinOperands(2);
        assertNotNull(check);
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(OrChainToSetContainsCheck.class, resource, NO_PROPS);
    }
}
