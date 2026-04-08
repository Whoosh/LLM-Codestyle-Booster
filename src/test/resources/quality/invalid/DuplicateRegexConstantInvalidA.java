package quality.invalid;

import java.util.regex.Pattern;

class DuplicateRegexConstantInvalidA {

    // String regex constants
    private static final String EMAIL_REGEX = "\\w+@\\w+\\.\\w+";
    private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String IP_REGEX = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";

    // Pattern constants
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    // Unique — never duplicated in other invalid files
    private static final String UNIQUE_REGEX = "\\s+hello\\s+";
}
