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
    private static final Map<String, String> DEFAULT_PROPS = Map.of("minChainLength", "4");

    @Test
    void invalidCasesProduceViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ChainedCallLineBreakCheck.class, "layout/invalid/ChainedCallInvalid.java", DEFAULT_PROPS);
        assertEquals(EXPECTED_VIOLATIONS, violations.size(), "Expected 2 chained call violations, got: " + format(violations));
    }

    @Test
    void validCasesProduceNoViolations() throws Exception {
        assertTrue(runTreeWalkerCheck(ChainedCallLineBreakCheck.class, "layout/valid/ChainedCallValid.java", DEFAULT_PROPS).isEmpty(), "Expected no violations");
    }

    @Test
    void setMinChainLengthAffectsThreshold() throws Exception {
        new ChainedCallLineBreakCheck().setMinChainLength(Integer.parseInt(MIN_CHAIN_3));
        List<AuditEvent> lowerThreshold = runTreeWalkerCheck(ChainedCallLineBreakCheck.class,
            "layout/invalid/ChainedCallInvalid.java", Map.of("minChainLength", MIN_CHAIN_3));
        assertFalse(lowerThreshold.isEmpty(), "Lower threshold should produce violations");
        assertTrue(lowerThreshold.size() >= EXPECTED_VIOLATIONS,
            "Lower threshold should produce at least as many violations as default");
    }

    @Test
    void edgeCaseValidFixtureProducesNoViolations() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ChainedCallLineBreakCheck.class,
            "layout/valid/ChainedCallEdgeCases.java", DEFAULT_PROPS);
        assertTrue(violations.isEmpty(), "Edge case valid fixture should produce no violations: " + format(violations));
    }

    @Test
    void splitMultiLineLongChainIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ChainedCallLineBreakCheck.class,
            "layout/valid/ChainedCallEdgeCases.java", DEFAULT_PROPS);
        assertTrue(violations.stream().noneMatch(v -> v.getLine() >= 6 && v.getLine() <= 12),
            "Multi-line long chains should not be flagged");
    }

    @Test
    void violationMessageContainsChainLength() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ChainedCallLineBreakCheck.class,
            "layout/invalid/ChainedCallInvalid.java", DEFAULT_PROPS);
        for (AuditEvent event : violations) {
            String msg = event.getMessage();
            assertTrue(msg.contains("4") || msg.contains("chain"),
                "Message should mention chain length: " + msg);
        }
    }

    @Test
    void innerMethodCallInOuterChainIsNotDoubleCounted() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ChainedCallLineBreakCheck.class,
            "layout/valid/ChainedCallEdgeCases.java", DEFAULT_PROPS);
        assertTrue(violations.isEmpty(),
            "Inner method calls in outer chains should not produce false positives: " + format(violations));
    }

    @Test
    void longChainProducesExactlyOneViolation() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(ChainedCallLineBreakCheck.class,
            "layout/invalid/ChainedCallLongChain.java", DEFAULT_PROPS);
        assertEquals(1, violations.size(),
            "An 8-call chain should produce exactly 1 violation (the outermost call), not more: " + format(violations));
    }
}
