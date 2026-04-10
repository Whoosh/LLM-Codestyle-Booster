package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class ChainedCallLineBreakCheckTest {

    private static final int EXPECTED_VIOLATIONS = 2;
    private static final String MIN_CHAIN_3 = "3";

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ChainedCallLineBreakCheck.class, "layout/invalid/ChainedCallInvalid.java", Map.of("minChainLength", "4"));
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 chained call violations, got: " + violations.size());
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(ChainedCallLineBreakCheck.class, "layout/valid/ChainedCallValid.java", Map.of("minChainLength", "4")).isEmpty(), "Expected no violations");
    }

    @Test
    void setMinChainLengthAffectsThreshold() throws Exception {
        new ChainedCallLineBreakCheck().setMinChainLength(Integer.parseInt(MIN_CHAIN_3));
        assertFalse(
            runTreeWalkerCheck(
                ChainedCallLineBreakCheck.class,
                "layout/invalid/ChainedCallInvalid.java",
                Map.of("minChainLength", MIN_CHAIN_3)).isEmpty(),
            "Lower threshold should produce violations");
    }
}
