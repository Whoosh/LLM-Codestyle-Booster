package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class UnrelatedNestedClassCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void unrelatedNestedClassesProduceViolations() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedClassInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
    }

    @Test
    void helperClassIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedClassInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Helper")), format(violations));
    }

    @Test
    void statsClassIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/UnrelatedNestedClassInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Stats")), format(violations));
    }

    @Test
    void classReferencingOuterIsIgnored() throws Exception {
        assertTrue(run("quality/valid/UnrelatedNestedClassValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(UnrelatedNestedClassCheck.class, resource, NO_PROPS);
    }
}
