package quality.invalid;

import java.util.regex.Pattern;

record DuplicateRegexConstantInvalidC(String value) {

    // Duplicates from A
    static final String DATE_VALIDATOR = "^\\d{4}-\\d{2}-\\d{2}$";
    static final Pattern WS_NORMALIZER = Pattern.compile("\\s+");

    // Duplicate from B
    static final String SHORT_RANGE = "\\d{2,4}";
}
