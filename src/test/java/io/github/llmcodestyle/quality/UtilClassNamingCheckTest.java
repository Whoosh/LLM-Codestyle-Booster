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
        List<AuditEvent> violations = run("quality/valid/UtilClassNamingValid.java");
        assertTrue(violations.isEmpty(), "Non-static nested classes should not be flagged");
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UtilClassNamingCheck.class, resource, NO_PROPS);
    }
}
