package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class StaticFinalFirstCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticFinalFirstCheck.class, "layout/invalid/StaticFinalFirstInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 violations (2 static finals after instance field), got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(StaticFinalFirstCheck.class, "layout/valid/StaticFinalFirstValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void violationMessageContainsFieldName() throws Exception {
        Set<String> messages = runTreeWalkerCheck(StaticFinalFirstCheck.class, "layout/invalid/StaticFinalFirstInvalid.java", NO_PROPS)
            .stream()
            .map(AuditEvent::getMessage)
            .collect(Collectors.toSet());
        assertTrue(messages.stream().anyMatch(m -> m.contains("CONSTANT_AFTER_INSTANCE")), "Should mention field name in message: " + messages);
        assertTrue(messages.stream().anyMatch(m -> m.contains("ANOTHER_CONST")), "Should mention field name in message: " + messages);
    }

    @Test
    void staticNonFinalFieldDoesNotTriggerBarrier() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticFinalFirstCheck.class, "layout/invalid/StaticFinalFirstEdgeCases.java", NO_PROPS);
        // Static non-final fields should not act as a barrier for the ordering check
        // The fixture has: static non-final, instance field, ctor, static final (flagged), more instance, static final (flagged), plus nested class
        assertTrue(violations.size() >= 2, "Expected at least 2 violations in edge case fixture: " + format(violations));
    }

    @Test
    void constructorActsAsBarrier() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(StaticFinalFirstCheck.class, "layout/invalid/StaticFinalFirstEdgeCases.java", NO_PROPS);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("AFTER_CTOR")), "static final after ctor should be flagged: " + format(violations));
    }
}
