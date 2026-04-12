package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TestMethodNameCheckTest {

    private static final int EXPECTED_VIOLATIONS = 4;
    private static final Map<String, String> NO_PROPS = Map.of();

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(TestMethodNameCheck.class, "quality/invalid/TestMethodNameInvalid.java", NO_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 4 underscore violations in test methods, got " + format(violations));
        for (AuditEvent event : violations) {
            assertTrue(event.getMessage().contains("camelCase"), "Expected camelCase mention in message, got: " + event.getMessage());
        }
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(TestMethodNameCheck.class, "quality/valid/TestMethodNameValid.java", NO_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void violationMessagesContainMethodName() throws Exception {
        for (AuditEvent event : runTreeWalkerCheck(TestMethodNameCheck.class, "quality/invalid/TestMethodNameInvalid.java", NO_PROPS)) {
            String msg = event.getMessage();
            assertTrue(msg.contains("_"), "Message should contain the offending method name with underscore: " + msg);
        }
    }

    @Test
    void nonTestMethodsWithUnderscoresAreNotFlagged() throws Exception {
        assertTrue(runTreeWalkerCheck(TestMethodNameCheck.class, "quality/valid/TestMethodNameValid.java", NO_PROPS).isEmpty(),
            "Non-test methods with underscores should not be flagged");
    }
}
