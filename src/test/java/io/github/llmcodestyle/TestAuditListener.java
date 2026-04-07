package io.github.llmcodestyle;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared AuditListener for custom check tests. Collects errors into a list.
 */
public final class TestAuditListener implements AuditListener {

    private final List<AuditEvent> violations;

    public TestAuditListener(List<AuditEvent> violations) {
        this.violations = violations;
    }

    @Override
    public void auditStarted(AuditEvent event) { }

    @Override
    public void auditFinished(AuditEvent event) { }

    @Override
    public void fileStarted(AuditEvent event) { }

    @Override
    public void fileFinished(AuditEvent event) { }

    @Override
    public void addError(AuditEvent event) {
        violations.add(event);
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        fail("Checkstyle exception: " + throwable.getMessage());
    }
}
