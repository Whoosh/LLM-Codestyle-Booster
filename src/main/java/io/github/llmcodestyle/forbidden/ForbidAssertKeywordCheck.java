package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Forbids the Java {@code assert} keyword. Use Guava {@code Preconditions} or explicit {@code if (...) throw} instead.
 */
public class ForbidAssertKeywordCheck extends AbstractCheck {

    static final String MSG_KEY = "forbid.assert.keyword";

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {LITERAL_ASSERT};
    }

    @Override
    public void visitToken(DetailAST ast) {
        log(ast.getLineNo(), MSG_KEY);
    }
}
