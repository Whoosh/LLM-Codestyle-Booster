package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstAnnotationUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Enforces camelCase naming for test methods annotated with JUnit/Jupiter test annotations. Runs alongside built-in MethodName check for targeted test guidance.
 */
public class TestMethodNameCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "test.method.name.camel.case";
    private static final int[] TOKENS = {METHOD_DEF};

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
        if (!AstAnnotationUtil.hasAnyAnnotationNamed(ast, "Test", "ParameterizedTest", "RepeatedTest", "TestFactory")) {
            return;
        }
        DetailAST ident = ast.findFirstToken(IDENT);
        if (ident == null) {
            return;
        }
        String name = ident.getText();
        if (name.contains("_")) {
            log(ident.getLineNo(), ident.getColumnNo(), MSG_KEY, name);
        }
    }

}
