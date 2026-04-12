package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MissingNullableParameterCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 5;

    @Test
    void invalidFileProducesExpectedViolationCount() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
    }

    @Test
    void nullCheckedParamWithReturnDefaultIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().contains("'text'") && v.getMessage().contains("'fixKatex'")),
            format(violations));
    }

    @Test
    void ternaryNullCheckIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().contains("'template'") && v.getMessage().contains("'format'")),
            format(violations));
    }

    @Test
    void notEqualNullGuardIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().contains("'data'") && v.getMessage().contains("'process'")),
            format(violations));
    }

    @Test
    void nullLiteralArgToUnhandledMethodIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertTrue(violations.stream().anyMatch(
            v -> v.getMessage().contains("Null literal") && v.getMessage().contains("'transform'")),
            format(violations));
    }

    @Test
    void validFileProducesNoViolations() throws Exception {
        assertTrue(run("quality/valid/MissingNullableParameterValid.java").isEmpty());
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(MissingNullableParameterCheck.class, resource, NO_PROPS);
    }
}
