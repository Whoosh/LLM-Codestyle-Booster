package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class IdenticalCatchBodyCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 2;

    @Test
    void identicalCatchBodiesProduceViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, runCheck("simplify/invalid/IdenticalCatchBodyInvalid.java").size());
    }

    @Test
    void differentCatchBodiesProduceNoViolations() throws Exception {
        assertTrue(runCheck("simplify/valid/IdenticalCatchBodyValid.java").isEmpty());
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(IdenticalCatchBodyCheck.class, resource, NO_PROPS);
    }
}
