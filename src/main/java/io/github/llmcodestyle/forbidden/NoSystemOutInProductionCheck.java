package io.github.llmcodestyle.forbidden;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.CLASS_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.DOT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.IDENT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.METHOD_CALL;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.PACKAGE_DEF;

/** Forbids {@code System.out/err} calls in production source. Exempt: Main/Application/Test classes and batch packages. */
public class NoSystemOutInProductionCheck extends AbstractCheck {

    /** Violation message key. */
    static final String MSG_KEY = "no.system.out.production";

    private static final String DOT_STR = "\\.";
    private static final String MAIN_SUFFIX = "Main";
    private static final String TEST_SUFFIX = "Test";
    private static final String SLOW_TEST_SUFFIX = "SlowTest";

    /** Simple class name for the file being checked. Updated per file. */
    private String simpleClassName = "";

    /** Package name for the file being checked. */
    private String pkgName = "";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{CLASS_DEF, PACKAGE_DEF, METHOD_CALL};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[]{CLASS_DEF, PACKAGE_DEF, METHOD_CALL};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]{CLASS_DEF, PACKAGE_DEF, METHOD_CALL};
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
        if (isExempt()) {
            return;
        }
        if (isSystemOutOrErrCall(methodCall)) {
            log(methodCall.getLineNo(), methodCall.getColumnNo(), MSG_KEY);
        }
    }

    private boolean isExempt() {
        if (simpleClassName.endsWith(MAIN_SUFFIX) || simpleClassName.startsWith(MAIN_SUFFIX) || simpleClassName.endsWith("Application")) {
            return true;
        }
        if (simpleClassName.endsWith(TEST_SUFFIX) || simpleClassName.endsWith(SLOW_TEST_SUFFIX)) {
            return true;
        }
        for (String segment : pkgName.split(DOT_STR)) {
            if ("batch".equals(segment)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSystemOutOrErrCall(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot == null) {
            return false;
        }
        DetailAST methodIdent = dot.getLastChild();
        if (methodIdent == null) {
            return false;
        }
        String methodName = methodIdent.getText();
        if (!"println".equals(methodName) && !"print".equals(methodName) && !"printf".equals(methodName) && !"format".equals(methodName)) {
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
