package simplify.valid;

import java.util.regex.Pattern;

public class RedundantConstantAliasMutationKiller2 {

    // simpleIdentInitializer: varDef with ASSIGN but EXPR has childCount != 1 (multi-child expression)
    // Exercises simpleIdentInitializer line 140: expr.getChildCount() != 1
    private static final int TOTAL = 1 + 2;

    // patternCompileValue: Pattern.compile with no ASSIGN (impossible in valid Java, skip)

    // patternCompileValue: methodCall that is NOT Pattern.compile
    private static final Pattern FROM_STATIC = Pattern.compile("unique_regex_" + "pattern");

    // patternCompileValue: elist with 0 EXPR children — Pattern.compile()
    // (Not valid Java — Pattern.compile requires at least 1 arg, so this isn't testable)

    // stringLiteralInitializer: varDef with ASSIGN -> EXPR -> non-STRING_LITERAL child
    private static final String COMPUTED = String.valueOf(42);

    // Field without ASSIGN
    private static final String NO_ASSIGN;

    static {
        NO_ASSIGN = "value";
    }

    // Non-Pattern, non-String static final — not checked for pattern duplication
    private static final int COUNT = 42;
}
