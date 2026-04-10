package simplify.invalid;

import java.util.regex.Pattern;

public class RedundantConstantAliasInvalid {

    private static final String WHITESPACE_REGEX = "\\s+";
    private static final String DIGITS_REGEX = "\\d+";

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_REGEX);
    private static final Pattern DIGITS_PATTERN = Pattern.compile("\\d+");

    private static final Pattern WHITESPACE_RUN = Pattern.compile(WHITESPACE_REGEX);
    private static final Pattern WHITESPACE_AGAIN = Pattern.compile("\\s+");

    private static final Pattern NUMBERS = Pattern.compile(DIGITS_REGEX);

    private static final Pattern PROBLEM_START = WHITESPACE_PATTERN;

    private static final String GREETING = "hello";
    private static final String HI = GREETING;
}
