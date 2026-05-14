package io.github.llmcodestyle;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import io.github.llmcodestyle.simplify.PureSingleUseLocalVariableCheck;
import io.github.llmcodestyle.simplify.SingleUseLocalVariableCheck;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that our simplify checks do not crash, hang, or worsen asymptotic complexity
 * when run against a stress fixture of trap patterns.
 *
 * <p>The test resource {@code AsymptoticSafetyTraps.java} contains 38+ trap patterns where
 * inlining a variable would move computation into a more-frequently-executed context
 * (for, for-each, while, do-while, lambda, stream, nested loops).
 *
 * <p>The dynamic loop {@link #everySimplifyCheckRunsOnTrapsWithoutCrashOrTimeout()} discovers
 * every {@code *Check.java} class under {@code io.github.llmcodestyle.simplify} via the
 * source tree, instantiates it, and runs it against the trap fixture under a 5-second
 * preemptive timeout. The goal is "doesn't crash, doesn't time out" — violation counts are
 * deliberately not asserted because many simplify checks legitimately fire on the trap
 * patterns (e.g. {@code IfReturnBooleanLiteralCheck} flags real boolean returns; the trap
 * fixture is constructed for the single-use checks only).
 *
 * <p>The two specific tests {@link #singleUseCheckDoesNotFlagLoopCachedVariables()} and
 * {@link #pureSingleUseCheckDoesNotFlagLoopCachedVariables()} retain the strict-zero
 * contract for the single-use family because that is the original asymptotic invariant the
 * fixture was designed for: the cached locals must NOT be inlined into the loop bodies.
 */
class AsymptoticSafetyTest {

    private static final String TRAPS_FILE = "valid/AsymptoticSafetyTraps.java";
    private static final String SIMPLIFY_PACKAGE_PREFIX = "io.github.llmcodestyle.simplify.";
    private static final String JAVA_EXT = ".java";
    private static final Path SIMPLIFY_SOURCE_DIR = Path.of("src/main/java/io/github/llmcodestyle/simplify");
    private static final Duration PER_CHECK_TIMEOUT = Duration.ofSeconds(5L);

    @Test
    void singleUseCheckDoesNotFlagLoopCachedVariables() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(SingleUseLocalVariableCheck.class, TRAPS_FILE, Map.of());
        assertTrue(violations.isEmpty(), "SingleUseLocalVariableCheck must not flag loop-cached variables (" + violations.size() + " violations): " + format(violations));
    }

    @Test
    void pureSingleUseCheckDoesNotFlagLoopCachedVariables() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PureSingleUseLocalVariableCheck.class, TRAPS_FILE, Map.of());
        assertTrue(violations.isEmpty(), "PureSingleUseLocalVariableCheck must not flag loop-cached variables (" + violations.size() + " violations): " + format(violations));
    }

    @Test
    void bothChecksTogetherProduceZeroOnTraps() throws Exception {
        List<AuditEvent> violations = runMultipleTreeWalkerChecks(
            Map.of(
                SingleUseLocalVariableCheck.class.getName(),
                Map.of(),
                PureSingleUseLocalVariableCheck.class.getName(),
                Map.of()),
            TRAPS_FILE);
        assertTrue(violations.isEmpty(), "Combined single-use checks must not flag any of 38 asymptotic traps (" + violations.size() + " violations): " + format(violations));
    }

    @Test
    void everySimplifyCheckRunsOnTrapsWithoutCrashOrTimeout() throws Exception {
        List<Class<?>> simplifyChecks = discoverSimplifyChecks();
        assertFalse(simplifyChecks.isEmpty(), "Failed to discover any simplify check classes under " + SIMPLIFY_SOURCE_DIR);
        assertAll(simplifyChecks.stream().<Executable>map(checkClass -> () -> assertTimeoutPreemptively(
            PER_CHECK_TIMEOUT,
            () -> runTreeWalkerCheck(checkClass, TRAPS_FILE, Map.of()),
            () -> String.format("%s exceeded %s on %s", checkClass.getSimpleName(), PER_CHECK_TIMEOUT, TRAPS_FILE))).toList());
    }

    /**
     * Discovers every {@code *Check.java} source file under {@link #SIMPLIFY_SOURCE_DIR} and
     * loads the corresponding {@link Class}. Source-tree discovery (rather than classpath
     * scanning) is used because it requires no extra dependency and is what
     * {@code CrossAnalyzerConsistencyTest} also relies on.
     */
    private static List<Class<?>> discoverSimplifyChecks() throws Exception {
        if (!Files.isDirectory(SIMPLIFY_SOURCE_DIR)) {
            return List.of();
        }
        List<Class<?>> classes = new ArrayList<>();
        try (Stream<Path> files = Files.list(SIMPLIFY_SOURCE_DIR)) {
            files
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith("Check.java"))
                .sorted()
                .forEach(source -> classes.add(loadCheckClass(source)));
        }
        return classes;
    }

    private static Class<?> loadCheckClass(Path source) {
        String simpleName = source.getFileName().toString();
        String fqn = SIMPLIFY_PACKAGE_PREFIX + simpleName.substring(0, simpleName.length() - JAVA_EXT.length());
        try {
            return Class.forName(fqn);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load simplify check class " + fqn, e);
        }
    }
}
