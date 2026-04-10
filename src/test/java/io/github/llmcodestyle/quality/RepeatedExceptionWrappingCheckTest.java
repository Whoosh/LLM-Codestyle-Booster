package io.github.llmcodestyle.quality;

import io.github.llmcodestyle.utils.TestCheckSupportUtil;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RepeatedExceptionWrappingCheckTest {

    private static final Map<String, String> NO_PROPS = Map.of();
    private static final int EXPECTED_VIOLATIONS = 3;

    @Test
    void repeatedWrappingProducesViolations() throws Exception {
        assertEquals(EXPECTED_VIOLATIONS, runCheck("quality/invalid/RepeatedExceptionWrappingInvalid.java").size());
    }

    @Test
    void differentWrappingProducesNoViolations() throws Exception {
        assertTrue(runCheck("quality/valid/RepeatedExceptionWrappingValid.java").isEmpty());
    }

    @Test
    void higherThresholdSuppressesViolations() throws Exception {
        new RepeatedExceptionWrappingCheck().setMinOccurrences(1);
        assertTrue(TestCheckSupportUtil.runTreeWalkerCheck(
            RepeatedExceptionWrappingCheck.class,
            "quality/invalid/RepeatedExceptionWrappingInvalid.java",
            Map.of("minOccurrences", "4")).isEmpty());
    }

    private static List<AuditEvent> runCheck(String resource) throws Exception {
        return TestCheckSupportUtil.runTreeWalkerCheck(RepeatedExceptionWrappingCheck.class, resource, NO_PROPS);
    }
}
