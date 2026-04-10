package simplify.valid;

import java.util.regex.Pattern;

public class RedundantConstantAliasValid {

    private static final String WHITESPACE_REGEX = "\\s+";
    private static final String DIGITS_REGEX = "\\d+";
    private static final String IDENTIFIER_REGEX = "[a-z]+";

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_REGEX);
    private static final Pattern DIGITS_PATTERN = Pattern.compile(DIGITS_REGEX);
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER_REGEX);

    private static final int MAX_LIMIT = 100;
    private static final int MIN_LIMIT = 1;

    private static final String GREETING = "hello";
    private static final String FAREWELL = "bye";

    private final String fieldFromConstant = GREETING;

    public Pattern reuseExisting() {
        return WHITESPACE_PATTERN;
    }

    public String localCopy() {
        String localAlias = GREETING;
        return localAlias;
    }
}
