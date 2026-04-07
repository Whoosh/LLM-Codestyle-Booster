package golden;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/** Golden test-scope file — must produce 0 violations when run through full checkstyle.xml. */
class IdempotencyGoldenTest {

    // AnnotationLocation strict: @TempDir on separate line for VARIABLE_DEF
    // Must NOT trigger UnnecessaryLineWrap (annotation skipped in computeFirstLine)
    @TempDir
    Path tempDir;

    // @Test on separate line — AnnotationLocation satisfied, no UnnecessaryLineWrap conflict
    @Test
    void verifyNotNull() {
        if (tempDir == null) {
            throw new IllegalArgumentException("tempDir must not be null");
        }
    }

    // ForbidAssertKeyword: no assert keyword — using if/throw instead
    @Test
    void verifyNonEmpty() {
        if (tempDir.toString().isEmpty()) {
            throw new IllegalStateException("tempDir path is empty");
        }
    }
}
