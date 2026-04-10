package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

/**
 * Enforces test class naming: top-level classes must end in {@code Test}/{@code SlowTest}, start with {@code Test}/{@code Abstract}, or be inner.
 */
public class TestClassNamingCheck extends AbstractCheck {

    /**
     * Violation message key for classes that don't follow the convention.
     */
    static final String MSG_KEY = "test.class.naming";
    private static final int[] TOKENS = {CLASS_DEF};

    private static final String TEST_KEYWORD = "Test";

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
        if (isInnerClass(ast)) {
            return;
        }
        DetailAST ident = ast.findFirstToken(IDENT);
        if (ident == null) {
            return;
        }
        String name = ident.getText();
        if (!isValidTestClassName(name)) {
            log(ident.getLineNo(), ident.getColumnNo(), MSG_KEY, name);
        }
    }

    private static boolean isValidTestClassName(String name) {
        return name.endsWith(TEST_KEYWORD) || name.endsWith("SlowTest") || name.startsWith(TEST_KEYWORD) || name.startsWith("Abstract");
    }
}
