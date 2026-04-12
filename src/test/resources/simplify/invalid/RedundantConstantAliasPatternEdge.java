package simplify.invalid;

import java.util.regex.Pattern;

public class RedundantConstantAliasPatternEdge {

    // String constants
    private static final String REGEX_A = "\\w+";
    private static final String REGEX_B = "\\d+";

    // Pattern compiled from string constant
    private static final Pattern PAT_A = Pattern.compile(REGEX_A);

    // Duplicate Pattern compiled from same constant reference — should be flagged
    private static final Pattern PAT_A_DUP = Pattern.compile(REGEX_A);

    // Pattern compiled from inline string literal
    private static final Pattern PAT_B = Pattern.compile("\\d+");

    // Duplicate Pattern compiled from different literal with same value as REGEX_B — should be flagged
    private static final Pattern PAT_B_DUP = Pattern.compile(REGEX_B);

    // Simple alias: same type, referencing sibling
    private static final String ALIAS_OF_A = REGEX_A;

    // Interface fields — implicitly static final
    interface Config {
        String TOKEN = "abc";
        String TOKEN_ALIAS = TOKEN;
    }
}
