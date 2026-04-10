package simplify.invalid;

public class CommonsLang3StringConstantInvalid {

    private static final String EMPTY = "";

    private static final String SPACE = " ";

    private static final String LF = "\n";

    private static final String CR = "\r";

    public static int total() {
        return EMPTY.length() + SPACE.length() + LF.length() + CR.length();
    }
}
