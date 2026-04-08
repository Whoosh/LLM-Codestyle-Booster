package quality.invalid;

import java.util.regex.Pattern;

class DuplicateRegexConstantInvalidWithinFile {

    // Two String constants with identical regex value
    private static final String VERSION_A = "\\d+\\.\\d+";
    private static final String VERSION_B = "\\d+\\.\\d+";

    // Two Pattern constants with identical regex value
    private static final Pattern ASSIGN_A = Pattern.compile("\\w+\\s*=\\s*\\w+");
    private static final Pattern ASSIGN_B = Pattern.compile("\\w+\\s*=\\s*\\w+");
}
