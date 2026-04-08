package golden;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Golden main-scope file — must produce 0 violations when run through full checkstyle.xml. */
public class IdempotencyGoldenMain {

    private static final String CONSTANT = "value";
    private static final int LIMIT = 10;

    private String field;

    // AnnotationLocation strict: @Override on separate line
    // Must NOT trigger UnnecessaryLineWrap (annotation skipped in computeFirstLine)
    @Override
    public String toString() {
        return CONSTANT + field;
    }

    // ForbidAssertKeyword fix pattern: multi-line if/throw with proper LeftCurly
    // Must NOT trigger LeftCurly (brace on same line) or NeedBraces
    void checkPrecondition(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("obj must not be null");
        }
    }

    // BlankLineAfterComment: comment directly above code, no blank line
    // Followed immediately by method — no violation
    void documentedMethod() throws IOException {
        try (var stream = getClass().getResourceAsStream(CONSTANT)) {
            if (stream != null) {
                stream.read();
            }
        }
    }

    // --- Chain vs Wrap conflict patterns ---
    // All 4+ chains broken across lines; all fit on 180 chars if joined.
    // Must NOT trigger UnnecessaryLineWrap.

    // Stream chain (4 calls): codePoints → mapToObj → toList → toString
    String streamChain() {
        return CONSTANT.codePoints()
            .mapToObj(Character::toString)
            .toList()
            .toString();
    }

    // String manipulation chain (4 calls): strip → toLowerCase → replace → trim
    String stringChain() {
        return CONSTANT.strip()
            .toLowerCase()
            .replace("a", "b")
            .trim();
    }

    // StringBuilder chain (5 calls): append x4 → toString
    String builderChain() {
        return new StringBuilder()
            .append(CONSTANT)
            .append("-")
            .append(field)
            .append("!")
            .toString();
    }

    // Optional chain (4 calls): ofNullable → map → filter → orElse
    String optionalChain() {
        return Optional.ofNullable(field)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .orElse(CONSTANT);
    }

    // Path chain (4 calls): of → resolve → getFileName → toString
    String pathChain() {
        return Path.of(CONSTANT)
            .resolve("sub")
            .getFileName()
            .toString();
    }

    // Stream with complex lambda (4 calls): stream → filter → map → toList
    List<String> streamWithLambda(List<String> items) {
        return items.stream()
            .filter(s -> s.length() > LIMIT)
            .map(String::toUpperCase)
            .toList();
    }

    // Chain inside return (method ref + lambda mix)
    String chainInReturn(List<String> items) {
        return items.stream()
            .filter(s -> !s.isEmpty())
            .map(String::trim)
            .reduce("", String::concat);
    }

    // --- MethodCallArguments vs UnnecessaryLineWrap ---
    // One-per-line args where joining fits in 180 chars but one-per-line is required
    // because mixed layout is forbidden. Golden file uses all-on-one-line.
    String methodArgsAllOnOneLine(String first, String second, String third) {
        return String.join("-", first, second, third);
    }

    // --- ConditionalReturnToTernary: already a ternary, no conflict ---
    String ternaryReturn(boolean flag) {
        return flag ? CONSTANT : field;
    }

    // 3-call chain on one line (below threshold — must NOT trigger ChainedCallLineBreak)
    String shortChainOnOneLine() {
        return CONSTANT.strip().trim().toLowerCase();
    }

    // Setter keeps 'field' from being flagged as unused by UnusedPrivateMembers
    void setField(String value) {
        this.field = value;
    }
}
