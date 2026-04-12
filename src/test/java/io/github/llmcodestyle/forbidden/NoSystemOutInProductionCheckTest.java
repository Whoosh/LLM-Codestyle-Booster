package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class NoSystemOutInProductionCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int PRODUCTION_CLASS_VIOLATIONS = 3;

    @Test
    void productionClassWithSystemOutProducesViolations() throws Exception {
        List<AuditEvent> violations = runCheck("forbidden/invalid/NoSystemOutProductionClass.java");
        assertEquals(PRODUCTION_CLASS_VIOLATIONS, violations.size(), "Expected 3 violations: " + format(violations));
    }

    @Test
    void innerMainClassDoesNotExemptOuterClass() throws Exception {
        List<AuditEvent> violations = runCheck("forbidden/invalid/NoSystemOutInnerMainTrap.java");
        assertEquals(1, violations.size(), "Inner Main-named class should not exempt outer: " + format(violations));
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

    @Test
    void applicationClassIsExempt() throws Exception {
        assertTrue(runCheck("forbidden/valid/NoSystemOutApplicationClass.java").isEmpty(),
            "Classes ending with 'Application' should be exempt");
    }

    @Test
    void mainPrefixClassIsExempt() throws Exception {
        assertTrue(runCheck("forbidden/valid/NoSystemOutMainPrefixClass.java").isEmpty(),
            "Classes starting with 'Main' should be exempt");
    }

    @Test
    void printfAndFormatAreFlagged() throws Exception {
        List<AuditEvent> violations = runCheck("forbidden/invalid/NoSystemOutPrintfFormatCall.java");
        assertEquals(2, violations.size(), "printf and format should be flagged: " + format(violations));
    }

    @Test
    void violationReportsCorrectLineAndColumn() throws Exception {
        List<AuditEvent> violations = runCheck("forbidden/invalid/NoSystemOutProductionClass.java");
        for (AuditEvent event : violations) {
            assertTrue(event.getLine() > 0, "Line should be positive");
            assertTrue(event.getColumn() >= 0, "Column should be non-negative");
        }
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(NoSystemOutInProductionCheck.class, resource, NO_PROPS);
    }
}
