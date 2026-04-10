package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MapContainsKeyThenGetCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 2;

    @Test
    void containsKeyThenGetProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, runCheck("simplify/invalid/MapContainsKeyThenGetInvalid.java").size());
    }

    @Test
    void getOrDefaultProducesNoViolations() throws Exception {
        assertTrue(runCheck("simplify/valid/MapContainsKeyThenGetValid.java").isEmpty());
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(MapContainsKeyThenGetCheck.class, resource, NO_PROPS);
    }
}
