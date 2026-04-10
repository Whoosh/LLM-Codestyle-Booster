package io.github.llmcodestyle.layout;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArrayInitSpaceCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final String INVALID = "layout/invalid/ArrayInitSpaceInvalid.java";
    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void arrayInitWithoutSpaceProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, runCheck(INVALID).size());
    }

    @Test
    void arrayInitWithSpaceProducesNoViolations() throws Exception {
        assertTrue(runCheck("layout/valid/ArrayInitSpaceValid.java").isEmpty());
    }

    @Test
    void messageIsDescriptive() throws Exception {
        assertFalse(runCheck(INVALID).isEmpty());
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(ArrayInitSpaceCheck.class, resource, NO_PROPS);
    }
}
