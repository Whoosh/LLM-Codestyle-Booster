package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UtilClassNamingCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void utilShapedAndConstantsViolationsAreFlagged() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("quality/invalid/UtilClassNamingInvalid.java").size());
    }

    @Test
    void wellNamedAndMixedClassesAreNotFlagged() throws Exception {
        assertTrue(run("quality/valid/UtilClassNamingValid.java").isEmpty());
    }

    @Test
    void violationMessageContainsClassName() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UtilClassNamingInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
        for (AuditEvent v : violations) {
            assertFalse(v.getMessage().isEmpty(), "Message should not be empty");
            assertTrue(v.getLine() > 0, "Line should be positive");
        }
    }

    @Test
    void nonStaticNestedClassIsNotFlagged() throws Exception {
        // isNonStaticNestedClass (line 88) checks isNestedType && !hasModifier(LITERAL_STATIC)
        // Non-static nested classes are skipped because they can't be util classes
        assertTrue(run("quality/valid/UtilClassNamingValid.java").isEmpty(), "Non-static nested classes should not be flagged");
    }

    @Test
    void nonStaticNestedWithStaticMethodsNotFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/valid/UtilClassNamingMutKiller.java");
        assertTrue(violations.isEmpty(), "Non-static nested class should not be flagged: " + format(violations));
    }

    @Test
    void mainEntryPointClassIsNotFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/valid/UtilClassNamingMainEntryPoint.java");
        assertTrue(violations.isEmpty(), "Class with public static main is a JVM entry point, not a utility: " + format(violations));
    }

    @Test
    void nestedUtilShapedClassIsFlaggedButOuterIsNot() throws Exception {
        // Guards against the bug where method-count state was held on the check
        // instance and the inner CLASS_DEF's counts overwrote the outer's before
        // the outer's evaluation completed. After the refactor, counts are local
        // to each visitToken call.
        List<AuditEvent> violations = run("quality/invalid/UtilClassNamingNestedInvalid.java");
        assertEquals(1, violations.size(), "Only the inner util-shaped class should be flagged: " + format(violations));
        AuditEvent only = violations.get(0);
        assertTrue(only.getMessage().contains("StringHelper"), "Violation should be raised against the inner class StringHelper, but was: " + only.getMessage());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UtilClassNamingCheck.class, resource, NO_PROPS);
    }
}
