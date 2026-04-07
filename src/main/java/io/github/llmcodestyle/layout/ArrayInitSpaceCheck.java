package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.ARRAY_INIT;

/** Ensures a space between {@code ]} and {@code &#123;} in array initializers: {@code int[] {1}} not {@code int[]{1}}. */
public class ArrayInitSpaceCheck extends AbstractCheck {

    /** Violation message key. */
    static final String MSG_KEY = "array.init.space";

    @Override
    public int[] getDefaultTokens() {
        return new int[] {ARRAY_INIT};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {ARRAY_INIT};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {ARRAY_INIT};
    }

    @Override
    public void visitToken(DetailAST ast) {
        int col = ast.getColumnNo();
        if (col <= 0) {
            return;
        }
        String line = getLines()[ast.getLineNo() - 1];
        if (col < line.length() && line.charAt(col - 1) == ']') {
            log(ast, MSG_KEY);
        }
    }
}
