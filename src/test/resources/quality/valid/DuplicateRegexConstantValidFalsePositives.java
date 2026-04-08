package quality.valid;

import java.util.regex.Pattern;

class DuplicateRegexConstantValidFalsePositives {

    // Windows file paths — \\d followed by alphanumeric, so not detected as regex
    private static final String FILE_PATH = "C:\\data\\files";
    private static final String ANOTHER_PATH = "D:\\documents\\work";
    private static final String DESKTOP_PATH = "C:\\Desktop\\stuff";

    // Java escape sequences — \\n, \\t, \\r are not in [dDwWsS], ignored
    private static final String NEWLINE = "line1\\nline2";
    private static final String TAB = "col1\\tcol2";
    private static final String RETURN = "before\\rafter";

    // Plain strings — no backslash sequences at all
    private static final String GREETING = "Hello World";
    private static final String DOTTED_NAME = "com.example.service";
    private static final String DASHED_NAME = "item-name-value";
    private static final String URL = "https://example.com/api/v1";

    // Concatenated regex — not a simple STRING_LITERAL, should be skipped
    private static final String CONCAT_REGEX = "\\d+" + "\\w+";

    // Non-static — check requires static + final
    private final String INSTANCE_REGEX = "\\d+";

    // Non-final — check requires static + final
    private static String MUTABLE_REGEX = "\\w+";

    // Non-string, non-pattern types — check ignores completely
    private static final int NUMERIC = 42;
    private static final boolean FLAG = true;
    private static final long MASK = 0xFF;

    // Pattern field without Pattern.compile — check cannot resolve
    private static final Pattern FROM_METHOD = Pattern.compile("unique-no-dup-\\d+");
}
