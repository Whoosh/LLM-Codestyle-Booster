package io.github.llmcodestyle.forbidden;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoSystemOutInProductionCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int PRODUCTION_CLASS_VIOLATIONS = 3;

    @Test
    void productionClassWithSystemOutProducesViolations() throws Exception {
        assertEquals(PRODUCTION_CLASS_VIOLATIONS, runCheck("forbidden/invalid/NoSystemOutProductionClass.java").size());
    }

    @Test
    void innerMainClassDoesNotExemptOuterClass() throws Exception {
        assertEquals(1, runCheck("forbidden/invalid/NoSystemOutInnerMainTrap.java").size());
    }

    @Test
    void mainClassIsExempt() throws Exception {
        assertTrue(runCheck("forbidden/valid/NoSystemOutMainClass.java").isEmpty());
    }

    @Test
    void testClassIsExempt() throws Exception {
        assertTrue(runCheck("forbidden/valid/NoSystemOutTestClass.java").isEmpty());
    }

    @Test
    void slowTestClassIsExempt() throws Exception {
        assertTrue(runCheck("forbidden/valid/NoSystemOutSlowTestClass.java").isEmpty());
    }

    @Test
    void batchPackageIsExempt() throws Exception {
        assertTrue(runCheck("forbidden/valid/NoSystemOutBatchPackage.java").isEmpty());
    }

    @Test
    void productionClassWithoutSystemOutPasses() throws Exception {
        assertTrue(runCheck("forbidden/valid/NoSystemOutNoSuchCalls.java").isEmpty());
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return TestCheckSupport.runTreeWalkerCheck(NoSystemOutInProductionCheck.class, resource, NO_PROPS);
    }
}
