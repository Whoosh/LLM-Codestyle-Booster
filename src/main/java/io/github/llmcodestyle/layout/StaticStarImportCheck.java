package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Requires static imports to use wildcard form.
 * Flags {@code import static com.foo.Bar.CONST} and requires {@code import static com.foo.Bar.*}.
 */
public class StaticStarImportCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "static.star.import";

    @Override
    public int[] getDefaultTokens() {
        return new int[] {STATIC_IMPORT};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {STATIC_IMPORT};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {STATIC_IMPORT};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST dot = ast.findFirstToken(DOT);
        if (dot == null) {
            return;
        }
        DetailAST lastChild = dot.getLastChild();
        if (lastChild != null && lastChild.getType() != STAR) {
            log(ast, MSG_KEY);
        }
    }
}
