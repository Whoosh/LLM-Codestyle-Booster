package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.TestCheckSupport;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IfReturnBooleanLiteralCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 4;

    @Test
    void ifReturnBooleansProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/IfReturnBooleanLiteralInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/IfReturnBooleanLiteralValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupport.runTreeWalkerCheck(IfReturnBooleanLiteralCheck.class, resource, NO_PROPS);
    }
}
