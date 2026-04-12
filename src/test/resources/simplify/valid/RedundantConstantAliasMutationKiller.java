package simplify.valid;

import java.util.regex.Pattern;

public class RedundantConstantAliasMutationKiller {

    // Pattern.compile with no args (elist has 0 exprs) — should not be flagged
    // Exercises patternCompileValue line 159: elist.getChildCount(EXPR) != 1

    // Pattern.compile with two args — should not be flagged
    private static final Pattern TWO_ARG = Pattern.compile("\\w+", Pattern.CASE_INSENSITIVE);

    // Non-static-final field referencing another — not flagged (isEffectivelyStaticFinal false)
    private static final String BASE = "hello";
    private String notStaticFinal = BASE;

    // Static but not final — not flagged
    private static String staticOnly = BASE;

    // Field with no ASSIGN — not flagged (simpleIdentInitializer line 136)
    private static final String NO_INIT;

    static {
        NO_INIT = "late";
    }

    // Pattern compile with null arg expression
    // This exercises patternCompileValue where argChild is neither STRING_LITERAL nor IDENT
    private static final Pattern FROM_EXPR = Pattern.compile("a" + "b");

    // Field with non-simple initializer (method call, not IDENT)
    private static final String COMPUTED = String.valueOf(42);

    // Interface field (implicitly static final) that's unique — no violation
    interface Config {
        String UNIQUE_TOKEN = "unique";
    }
}
