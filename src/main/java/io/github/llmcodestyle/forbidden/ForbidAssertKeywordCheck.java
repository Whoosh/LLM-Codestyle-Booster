package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Forbids the Java {@code assert} keyword. Use Guava {@code Preconditions} or explicit {@code if (...) throw} instead.
 */
public class ForbidAssertKeywordCheck extends AbstractCheck {

    static final String MSG_KEY = "forbid.assert.keyword";
    private static final int[] TOKENS = {LITERAL_ASSERT};

    @Override
    public int[] getDefaultTokens() {
        return TOKENS.clone();
    }

    @Override
    public int[] getAcceptableTokens() {
        return TOKENS.clone();
    }

    @Override
    public int[] getRequiredTokens() {
        return TOKENS.clone();
    }

    @Override
    public void visitToken(DetailAST ast) {
        log(ast.getLineNo(), MSG_KEY);
    }
}
