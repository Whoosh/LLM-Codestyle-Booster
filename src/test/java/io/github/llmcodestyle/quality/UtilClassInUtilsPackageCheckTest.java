package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.TestCheckSupport;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UtilClassInUtilsPackageCheckTest {

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = TestCheckSupport.runTreeWalkerCheck(UtilClassInUtilsPackageCheck.class, "quality/invalid/UtilClassInUtilsPackageInvalid.java", Map.of());
        assertEquals(1, violations.size(), "Expected 1 wrong-package violation, got " + violations.size());
        assertTrue(violations.get(0).getMessage().contains("PhysicsTextUtil"), "Expected class name in message");
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(
            TestCheckSupport.runTreeWalkerCheck(
                UtilClassInUtilsPackageCheck.class,
                "quality/valid/UtilClassInUtilsPackageValid.java",
                Map.of()).isEmpty(),
            "Expected no violations");
    }
}
