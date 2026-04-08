package quality.invalid;

enum DuplicateRegexConstantInvalidD {

    INSTANCE;

    // Duplicate from A
    static final String IP_VALIDATOR = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
}
