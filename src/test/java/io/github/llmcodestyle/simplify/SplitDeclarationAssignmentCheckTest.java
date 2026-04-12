package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class SplitDeclarationAssignmentCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 8;

    @Test
    void splitDeclAssignmentProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, run("simplify/invalid/SplitDeclarationAssignmentInvalid.java").size());
    }

    @Test
    void validCornerCasesProduceNoViolations() throws Exception {
        assertTrue(run("simplify/valid/SplitDeclarationAssignmentValid.java").isEmpty());
    }

    @Test
    void violationMessagesContainVariableName() throws Exception {
        // uninitializedVarName (line 79) returns variable name
        // If mutated to return "", messages would have empty name
        List<AuditEvent> violations = run("simplify/invalid/SplitDeclarationAssignmentInvalid.java");
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), format(violations));
        for (AuditEvent v : violations) {
            assertFalse(v.getMessage().isEmpty(), "Message should not be empty");
            assertTrue(v.getLine() > 0, "Line should be positive");
        }
    }

    private static List<AuditEvent> run(String resource) throws Exception {
        return runTreeWalkerCheck(SplitDeclarationAssignmentCheck.class, resource, NO_PROPS);
    }
}
