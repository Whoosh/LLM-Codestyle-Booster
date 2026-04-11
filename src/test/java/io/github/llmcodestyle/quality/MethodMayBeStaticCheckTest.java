package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MethodMayBeStaticCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_NON_FINAL_CLASS_VIOLATIONS = 7;
    private static final int EXPECTED_FINAL_CLASS_VIOLATIONS = 3;

    @Test
    void statelessPrivateAndFinalMethodsInNonFinalClassAreFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MethodMayBeStaticInvalid.java");
        assertEquals(EXPECTED_NON_FINAL_CLASS_VIOLATIONS, violations.size(), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("buildUserMessage")), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("finalHelper")), format(violations));
    }

    @Test
    void publicAndPackageStatelessMethodsInFinalClassAreFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MethodMayBeStaticFinalClassInvalid.java");
        assertEquals(EXPECTED_FINAL_CLASS_VIOLATIONS, violations.size(), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("buildUserMessage")), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("formatGreeting")), format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("doubleOf")), format(violations));
        assertFalse(violations.stream().anyMatch(v -> v.getMessage().contains("systemPrompt")), format(violations));
    }

    @Test
    void instanceMethodsAndShadowingAndNestedNonStaticAreNotFlagged() throws Exception {
        assertTrue(run("quality/valid/MethodMayBeStaticValid.java").isEmpty(), "expected no violations for instance-referencing methods and overridable non-final-class methods");
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(MethodMayBeStaticCheck.class, resource, NO_PROPS);
    }

    private static String format(List<AuditEvent> events) {
        return events.stream()
            .map(e -> "\n  Line " + e.getLine() + ": " + e.getMessage())
            .toList()
            .toString();
    }
}
