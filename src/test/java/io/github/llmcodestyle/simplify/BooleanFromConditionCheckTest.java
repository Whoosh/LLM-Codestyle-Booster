package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(BooleanFromConditionCheck.class, resource, NO_PROPS);
    }
}
