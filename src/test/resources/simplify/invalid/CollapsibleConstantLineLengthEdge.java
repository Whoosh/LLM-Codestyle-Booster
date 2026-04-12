package test;

public class CollapsibleConstantLineLengthEdge {

    // This field has a very long prefix (indentation + modifiers + name + "= ") so that
    // the merged literal would exceed MAX_LINE_LENGTH (180). The check should still fire
    // because the merge contains non-string operands (nums) which make contentLength < 0.
    static final int SUM_OF_NUMS = 10 + 20;

    // String concatenation that is well within line length
    static final String SHORT = "a" + "b";

    // Array created with 'new String[] { ... }' syntax — exercises LITERAL_NEW -> ARRAY_INIT path
    static final String[] EXPLICIT_NEW_ARRAY = new String[] {
        "alpha" + "_bravo",
        "charlie" + "_delta",
    };

    // Concatenation where merged result is all strings and fits easily
    static final String FIT_EASY = "Hello" + " World";

    // Interface fields are implicitly static final
    interface LongNames {
        String F1 = "x";
        String F2 = "y";
        String COMBO = F1 + F2;
    }

    // EXPR wrapping in computeMergedStringContentLength
    static final String EXPR_WRAP = ("Hello" + " there");

    // Non-PLUS, non-STRING_LITERAL, non-EXPR node under a PLUS: contentLength returns -1
    // This triggers the type != STRING_LITERAL (line 161) and type != EXPR (line 164) and type != PLUS (line 167)
    static final String MIXED = "prefix" + 42;
}
