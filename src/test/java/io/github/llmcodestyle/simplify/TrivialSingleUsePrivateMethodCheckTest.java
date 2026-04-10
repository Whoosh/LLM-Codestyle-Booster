package io.github.llmcodestyle.simplify;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    private static List<AuditEvent> run(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(TrivialSingleUsePrivateMethodCheck.class, resource, NO_PROPS);
    }
}
