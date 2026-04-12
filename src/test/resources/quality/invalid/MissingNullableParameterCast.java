package quality.invalid;

import jakarta.annotation.Nullable;

public class MissingNullableParameterCast {

    // Null literal passed via typecast — exercises isNullLiteralExpr TYPECAST path
    public void callWithCastNull() {
        process((String) null);
    }

    private String process(String input) {
        return input.toUpperCase();
    }

    // Reversed null comparison: null == param (exercises matchesNullComparison right-to-left)
    public String reversed(String text) {
        if (null == text) {
            return "empty";
        }
        return text;
    }
}
