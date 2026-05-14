package simplify.valid;

import java.util.regex.Pattern;

public class RedundantConstantAliasMutationKiller {

    // === All static final FIRST ===

    // Pattern.compile with two args — should not be flagged
    private static final Pattern TWO_ARG = Pattern.compile("\\w+", Pattern.CASE_INSENSITIVE);

    // BASE used by other constants and instance fields
    private static final String BASE = "hello";

    // Field with no ASSIGN — not flagged (simpleIdentInitializer line 136)
    private static final String NO_INIT;

    // Pattern compile with concatenation expression
    // Exercises patternCompileValue where argChild is neither STRING_LITERAL nor IDENT
    private static final Pattern FROM_EXPR = Pattern.compile("a" + BASE);

    // Field with non-simple initializer (method call, not IDENT)
    private static final String COMPUTED = String.valueOf(42);

    static {
        NO_INIT = "late";
    }

    // === Static non-final ===
    // Static but not final — not flagged
    private static String staticOnly = BASE;

    // === Instance fields ===
    // Non-static-final field referencing another — not flagged (isEffectivelyStaticFinal false)
    private String notStaticFinal = BASE;

    // Interface field (implicitly static final) that's unique — no violation
    interface Config {
        String UNIQUE_TOKEN = "unique";
    }
}
