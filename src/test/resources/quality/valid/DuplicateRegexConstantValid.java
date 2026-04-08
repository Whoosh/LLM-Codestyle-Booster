package quality.valid;

import java.util.regex.Pattern;

class DuplicateRegexConstantValid {

    // Each regex here is unique — no duplicates within or across files
    private static final String EMAIL_REGEX = "\\w+@\\w+\\.\\w+";
    private static final Pattern DIGITS = Pattern.compile("\\d+");
    private static final String NOT_A_REGEX = "Hello World";
    private static final String FILE_PATH = "C:\\data\\files";
    private static final int CONSTANT = 42;
}
