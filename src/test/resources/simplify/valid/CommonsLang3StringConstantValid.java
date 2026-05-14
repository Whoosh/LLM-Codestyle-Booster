package simplify.valid;

public class CommonsLang3StringConstantValid {

    // Domain-specific constant — no StringUtils equivalent.
    private static final String SEPARATOR = ",";

    // Multi-character constant — not a single whitespace, no equivalent.
    private static final String NEWLINE_TWO = "\n\n";

    // Tab is not in StringUtils — no flag.
    private static final String TAB = "\t";

    // Local variable, not a static final field — should not be flagged.
    public static String localEmpty() {
        return "";
    }

    // Static final but not String — should not be flagged.
    private static final int LIMIT = 0;

    // Static final String with no initializer — exercises stringLiteralInitText null ASSIGN path
    private static final String UNINITIALIZED;

    static {
        UNINITIALIZED = "";
    }

    // Instance field (not static) — outside the rule.
    private final String space = " ";

    public String describe() {
        return space + LIMIT + describeRest();
    }

    private String describeRest() {
        if (TAB.isEmpty()) {
            throw new IllegalStateException();
        }
        return SEPARATOR + Integer.toString(LIMIT) + TAB + String.valueOf(LIMIT) + NEWLINE_TWO;
    }
}
