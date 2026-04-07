package io.github.llmcodestyle.layout;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayInitSpaceCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void arrayInitWithoutSpaceProducesViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            ArrayInitSpaceCheck.class, "layout/invalid/ArrayInitSpaceInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size());
    }

    @Test
    void arrayInitWithSpaceProducesNoViolations() throws Exception {
        assertTrue(TestCheckSupport.runTreeWalkerCheck(
            ArrayInitSpaceCheck.class, "layout/valid/ArrayInitSpaceValid.java", NO_PROPS).isEmpty());
    }

    @Test
    void messageIsDescriptive() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(
            ArrayInitSpaceCheck.class, "layout/invalid/ArrayInitSpaceInvalid.java", NO_PROPS);
        assertTrue(violations.getFirst().getMessage().contains("space"));
    }
}
