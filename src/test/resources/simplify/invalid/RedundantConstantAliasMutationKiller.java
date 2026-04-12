package simplify.invalid;

import java.util.regex.Pattern;

public class RedundantConstantAliasMutationKiller {

    // String constants for Pattern.compile testing
    private static final String REGEX_X = "\\w+";

    // Pattern compiled from REGEX_X
    private static final Pattern PAT_X = Pattern.compile(REGEX_X);

    // Duplicate Pattern compiled from same constant — should be flagged (patternCompileValue via IDENT)
    private static final Pattern PAT_X_DUP = Pattern.compile(REGEX_X);

    // Simple ident alias of same type — exercises simpleIdentInitializer line 140 (expr.getChildCount() != 1)
    private static final String ALIAS_X = REGEX_X;

    // Pattern compiled from literal — exercises patternCompileValue STRING_LITERAL path (line 167)
    private static final Pattern PAT_LITERAL = Pattern.compile("\\d+");

    // Duplicate pattern from same literal value — should be flagged
    private static final Pattern PAT_LITERAL_DUP = Pattern.compile("\\d+");

    // This exercises stringLiteralInitializer line 178: first != null && first.getType() == STRING_LITERAL
    // STRING constant used to detect aliases
    private static final String GREETING = "hello";
    private static final String GREETING_ALIAS = GREETING;
}
