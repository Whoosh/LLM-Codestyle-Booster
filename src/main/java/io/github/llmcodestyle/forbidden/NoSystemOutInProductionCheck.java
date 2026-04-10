package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;
import io.github.llmcodestyle.utils.AstMethodCallUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Forbids {@code System.out/err} calls in production source. Exempt: Main/Application/Test classes and batch packages.
 */
public class NoSystemOutInProductionCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "no.system.out.production";
    private static final int[] TOKENS = {CLASS_DEF, PACKAGE_DEF, METHOD_CALL};

    private static final String DOT_STR = "\\.";
    private static final String MAIN_SUFFIX = "Main";
    private static final String TEST_SUFFIX = "Test";
    private static final String SLOW_TEST_SUFFIX = "SlowTest";

    /**
     * Simple class name for the file being checked. Updated per file.
     */
    private String simpleClassName = "";

    /**
     * Package name for the file being checked.
     */
    private String pkgName = "";

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
    public void beginTree(DetailAST rootAST) {
        simpleClassName = "";
        pkgName = "";
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == PACKAGE_DEF) {
            pkgName = AstUtil.extractPackageName(ast);
        } else if (ast.getType() == CLASS_DEF) {
            if (!AstUtil.isInnerClass(ast)) {
                DetailAST ident = ast.findFirstToken(IDENT);
                if (ident != null) {
                    simpleClassName = ident.getText();
                }
            }
        } else if (ast.getType() == METHOD_CALL) {
            checkMethodCall(ast);
        }
    }

    private void checkMethodCall(DetailAST methodCall) {
        if (!isExempt() && isSystemOutOrErrCall(methodCall)) {
            log(methodCall.getLineNo(), methodCall.getColumnNo(), MSG_KEY);
        }
    }

    private boolean isExempt() {
        if (isMainOrTestClassName()) {
            return true;
        }
        for (String segment : pkgName.split(DOT_STR)) {
            if ("batch".equals(segment)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMainOrTestClassName() {
        return simpleClassName.endsWith(MAIN_SUFFIX)
            || simpleClassName.startsWith(MAIN_SUFFIX)
            || simpleClassName.endsWith("Application")
            || simpleClassName.endsWith(TEST_SUFFIX)
            || simpleClassName.endsWith(SLOW_TEST_SUFFIX);
    }

    private static boolean isSystemOutOrErrCall(DetailAST methodCall) {
        String methodName = AstMethodCallUtil.extractMethodName(methodCall);
        if (!"println".equals(methodName) && !"print".equals(methodName) && !"printf".equals(methodName) && !"format".equals(methodName)) {
            return false;
        }
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot == null) {
            return false;
        }
        DetailAST receiverDot = dot.getFirstChild();
        if (receiverDot == null || receiverDot.getType() != DOT) {
            return false;
        }
        DetailAST systemIdent = receiverDot.getFirstChild();
        DetailAST streamIdent = receiverDot.getLastChild();
        if (systemIdent == null || streamIdent == null) {
            return false;
        }
        return "System".equals(systemIdent.getText()) && ("out".equals(streamIdent.getText()) || "err".equals(streamIdent.getText()));
    }
}
