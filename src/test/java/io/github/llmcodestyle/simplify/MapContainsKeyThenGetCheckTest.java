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
    private static final int LINE_ELSE_BRANCH = 19;

    @Test
    void containsKeyThenGetProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, runCheck("simplify/invalid/MapContainsKeyThenGetInvalid.java").size());
    }

    @Test
    void getOrDefaultProducesNoViolations() throws Exception {
        assertTrue(runCheck("simplify/valid/MapContainsKeyThenGetValid.java").isEmpty());
    }

    @Test
    void getInElseBranchIsFlagged() throws Exception {
        // containsGetCallOnSameReceiver line 77: else branch path
        List<AuditEvent> violations = runCheck("simplify/invalid/MapContainsKeyThenGetElse.java");
        // negatedContainsKey has get in else block — should be flagged
        assertTrue(violations.stream().anyMatch(v -> v.getLine() >= LINE_ELSE_BRANCH), "get in else branch should be flagged: " + format(violations));
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return runTreeWalkerCheck(MapContainsKeyThenGetCheck.class, resource, NO_PROPS);
    }
}
