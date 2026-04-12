package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class ForbiddenGenericCatchCheckTest {

    private static final int EXPECTED_VIOLATIONS = 6;
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ForbiddenGenericCatchCheck.class, "forbidden/invalid/ForbiddenGenericCatchInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 6 generic catch violations, got: " + format(violations));
        for (AuditEvent event : violations) {
            assertTrue(event.getMessage().contains("forbidden"), "Unexpected message: " + event.getMessage());
        }
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(ForbiddenGenericCatchCheck.class, "forbidden/valid/ForbiddenGenericCatchValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void allThreeForbiddenTypesAreCaught() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ForbiddenGenericCatchCheck.class, "forbidden/invalid/ForbiddenGenericCatchInvalid.java", NO_PROPS);
        Set<String> messages = violations.stream().map(AuditEvent::getMessage).collect(Collectors.toSet());
        assertTrue(messages.stream().anyMatch(m -> m.contains("Exception")), "Should detect Exception: " + messages);
        assertTrue(messages.stream().anyMatch(m -> m.contains("Throwable")), "Should detect Throwable: " + messages);
        assertTrue(messages.stream().anyMatch(m -> m.contains("RuntimeException")), "Should detect RuntimeException: " + messages);
    }

    @Test
    void fullyQualifiedCatchTypeIsFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ForbiddenGenericCatchCheck.class, "forbidden/invalid/ForbiddenGenericCatchInvalid.java", NO_PROPS);
        // Lines 41 and 49 have fully qualified catches
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 41),
            "Fully-qualified java.lang.Exception should be flagged on line 41: " + format(violations));
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 49),
            "Fully-qualified java.lang.Throwable should be flagged on line 49: " + format(violations));
    }

    @Test
    void multiCatchWithGenericTypeIsFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ForbiddenGenericCatchCheck.class, "forbidden/invalid/ForbiddenGenericCatchInvalid.java", NO_PROPS);
        // Line 32 has multi-catch with Exception
        assertTrue(violations.stream().anyMatch(v -> v.getLine() == 32),
            "Multi-catch with generic type should be flagged on line 32: " + format(violations));
    }
}
