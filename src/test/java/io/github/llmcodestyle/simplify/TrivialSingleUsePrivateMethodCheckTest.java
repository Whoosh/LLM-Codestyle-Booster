package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TrivialSingleUsePrivateMethodCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void singleUseTrivialPrivatesAreFlagged() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/TrivialSingleUsePrivateMethodInvalid.java").size());
    }

    @Test
    void overloadsRecursionAndMultiUseAreNotFlagged() throws Exception {
        assertTrue(run("simplify/valid/TrivialSingleUsePrivateMethodValid.java").isEmpty());
    }

    @Test
    void violationMessagesAreDescriptive() throws Exception {
        // visitToken line 97: negated conditional on countIdent check
        List<AuditEvent> violations = run("simplify/invalid/TrivialSingleUsePrivateMethodInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
        for (AuditEvent v : violations) {
            assertFalse(v.getMessage().isEmpty(), "Message should not be empty");
            assertTrue(v.getLine() > 0, "Line should be positive");
        }
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(TrivialSingleUsePrivateMethodCheck.class, resource, NO_PROPS);
    }
}
