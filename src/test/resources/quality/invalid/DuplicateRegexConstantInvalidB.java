package quality.invalid;

import java.util.regex.Pattern;

class DuplicateRegexConstantInvalidB {

    // Duplicates from A (String + Pattern)
    private static final String MAIL_PATTERN = "\\w+@\\w+\\.\\w+";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    // Unique to B — duplicated later in C
    private static final String RANGE_REGEX = "\\d{2,4}";
}
