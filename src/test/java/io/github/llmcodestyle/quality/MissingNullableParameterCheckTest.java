package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class MissingNullableParameterCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 6;

    @Test
    void invalidFileProducesExpectedViolationCount() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
    }

    @Test
    void nullCheckedParamWithReturnDefaultIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("'text'") && v.getMessage().contains("'fixKatex'")), format(violations));
    }

    @Test
    void ternaryNullCheckIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("'template'") && v.getMessage().contains("'format'")), format(violations));
    }

    @Test
    void notEqualNullGuardIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("'data'") && v.getMessage().contains("'process'")), format(violations));
    }

    @Test
    void nullLiteralArgToUnhandledMethodIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Null literal") && v.getMessage().contains("'transform'")), format(violations));
    }

    @Test
    void validFileProducesNoViolations() throws Exception {
        assertTrue(run("quality/valid/MissingNullableParameterValid.java").isEmpty());
    }

    @Test
    void typecastNullLiteralArgIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterCast.java");
        // (String) null exercises isNullLiteralExpr TYPECAST path (line 285)
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Null literal")), "Cast null literal should be flagged: " + format(violations));
    }

    @Test
    void reversedNullComparisonIsFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterCast.java");
        // null == text exercises matchesNullComparison reversed path (line 225)
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("'text'") && v.getMessage().contains("'reversed'")),
            "Reversed null comparison should be flagged: " + format(violations));
    }

    @Test
    void compoundLandRejectionIsNotFlagged() throws Exception {
        // 's == null && verbose' followed by throw is still a rejection -> no violation
        assertTrue(run("quality/valid/MissingNullableParameterValid.java").isEmpty());
    }

    @Test
    void nestedThrowInsideTryIsNotFlagged() throws Exception {
        // throw inside nested try-block of if(s == null) is still a rejection -> no violation
        assertTrue(run("quality/valid/MissingNullableParameterValid.java").isEmpty());
    }

    @Test
    void compoundLorIsStillFlagged() throws Exception {
        List<AuditEvent> violations = run("quality/invalid/MissingNullableParameterInvalid.java");
        // 'maybe == null || other' does not guarantee null -> still missing @Nullable
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("'maybe'") && v.getMessage().contains("'lorNotRejection'")),
            "LOR-guarded parameter should still be flagged: " + format(violations));
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(MissingNullableParameterCheck.class, resource, NO_PROPS);
    }
}
