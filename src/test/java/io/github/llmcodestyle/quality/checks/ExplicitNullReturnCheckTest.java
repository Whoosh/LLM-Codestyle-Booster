package io.github.llmcodestyle.quality.checks;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class ExplicitNullReturnCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void explicitNullReturnsProduceViolations() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ExplicitNullReturnInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
    }

    @Test
    void directNullReturnIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ExplicitNullReturnInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("findName")), format(violations));
    }

    @Test
    void nullableDelegationIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ExplicitNullReturnInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("getName") && v.getMessage().contains("findNullable")), format(violations));
    }

    @Test
    void qualifiedThisDelegationIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/ExplicitNullReturnInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("getNameQualified") && v.getMessage().contains("findNullable")), format(violations));
    }

    @Test
    void validCodeProducesNoViolations() throws Exception {
        assertTrue(run("quality/valid/ExplicitNullReturnValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(ExplicitNullReturnCheck.class, resource, NO_PROPS);
    }
}
