package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnrelatedNestedInterfaceCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 2;

    @Test
    void unrelatedNestedInterfacesProduceViolations() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedInterfaceInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
    }

    @Test
    void formatterInterfaceIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedInterfaceInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Formatter")), format(violations));
    }

    @Test
    void callbackInterfaceIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedInterfaceInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Callback")), format(violations));
    }

    @Test
    void interfaceReferencingOuterConstantIsIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedInterfaceValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UnrelatedNestedInterfaceCheck.class, resource, NO_PROPS);
    }
}
