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

    @Test
    void productionClassWithSystemOutProducesViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            NoSystemOutInProductionCheck.class, "invalid/NoSystemOutProductionClass.java", NO_PROPS);
        assertEquals(3, violations.size());
    }

    @Test
    void innerMainClassDoesNotExemptOuterClass() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            NoSystemOutInProductionCheck.class, "invalid/NoSystemOutInnerMainTrap.java", NO_PROPS);
        assertEquals(1, violations.size());
    }

    @Test
    void mainClassIsExempt() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            NoSystemOutInProductionCheck.class, "valid/NoSystemOutMainClass.java", NO_PROPS);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testClassIsExempt() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            NoSystemOutInProductionCheck.class, "valid/NoSystemOutTestClass.java", NO_PROPS);
        assertTrue(violations.isEmpty());
    }

    @Test
    void slowTestClassIsExempt() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            NoSystemOutInProductionCheck.class, "valid/NoSystemOutSlowTestClass.java", NO_PROPS);
        assertTrue(violations.isEmpty());
    }

    @Test
    void batchPackageIsExempt() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            NoSystemOutInProductionCheck.class, "valid/NoSystemOutBatchPackage.java", NO_PROPS);
        assertTrue(violations.isEmpty());
    }

    @Test
    void productionClassWithoutSystemOutPasses() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            NoSystemOutInProductionCheck.class, "valid/NoSystemOutNoSuchCalls.java", NO_PROPS);
        assertTrue(violations.isEmpty());
    }
}
