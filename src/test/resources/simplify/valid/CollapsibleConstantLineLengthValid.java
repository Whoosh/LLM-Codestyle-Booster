package test;

public class CollapsibleConstantLineLengthValid {

    // This concatenation is all strings but the merged result would exceed 180 chars.
    // declarationPrefixLength + 2 (quotes) + content + 1 (quote) > 180
    // The 'static final String' plus name plus '= ' already takes ~45 chars.
    // We need the merged content to push past 180 total.
    static final String VERY_LONG_CONCAT = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";

    // Array with no concatenation in elements
    private static final String[] CLEAN = {"one", "two"};

    // Method body with single constant between dynamics - below threshold of 2
    static String noRun() {
        return dynamicValue() + "single" + dynamicValue();
    }

    private static String dynamicValue() {
        return "x";
    }
}
