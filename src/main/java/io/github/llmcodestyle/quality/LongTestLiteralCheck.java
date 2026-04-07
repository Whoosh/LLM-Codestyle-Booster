package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstAnnotationUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Flags long string literals in test method bodies. Strings longer than {@code maxLength} (default 30) should be extracted to resources.
 */
public class LongTestLiteralCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "long.test.literal";

    private static final int DEFAULT_MAX_LENGTH = 30;

    private int maxLength = DEFAULT_MAX_LENGTH;

    /**
     * Set the maximum allowed string literal length.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

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
        return new int[] {STRING_LITERAL};
    }

    @Override
    public void visitToken(DetailAST ast) {
        int contentLen = ast.getText().length() - 2;
        if (contentLen <= maxLength) {
            return;
        }
        if (!isInsideTestMethod(ast)) {
            return;
        }
        if (isFieldInitializer(ast)) {
            return;
        }
        if (isInsideDisplayName(ast)) {
            return;
        }
        if (isAssertionMessage(ast)) {
            return;
        }
        log(ast.getLineNo(), ast.getColumnNo(), MSG_KEY, contentLen, maxLength);
    }

    private static boolean isInsideTestMethod(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == METHOD_DEF) {
                return AstAnnotationUtil.hasAnyAnnotationNamed(parent, "Test", "ParameterizedTest", "RepeatedTest", "TestFactory", "MethodSource");
            }
            parent = parent.getParent();
        }
        return false;
    }

    private static boolean isFieldInitializer(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == VARIABLE_DEF) {
                DetailAST grandParent = parent.getParent();
                if (grandParent != null && grandParent.getType() == OBJBLOCK) {
                    return true;
                }
            }
            if (parent.getType() == METHOD_DEF) {
                break;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private static boolean isInsideDisplayName(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == ANNOTATION) {
                DetailAST ident = parent.findFirstToken(IDENT);
                if (ident != null && "DisplayName".equals(ident.getText())) {
                    return true;
                }
            }
            if (parent.getType() == METHOD_DEF || parent.getType() == CLASS_DEF) {
                break;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private static boolean isAssertionMessage(DetailAST ast) {
        DetailAST node = ast.getParent();
        while (node != null && node.getType() != EXPR && node.getType() != METHOD_DEF && node.getType() != LAMBDA) {
            node = node.getParent();
        }
        if (node == null || node.getType() != EXPR) {
            return false;
        }
        DetailAST elist = node.getParent();
        if (elist == null || elist.getType() != ELIST) {
            return false;
        }
        DetailAST methodCall = elist.getParent();
        if (methodCall == null || methodCall.getType() != METHOD_CALL) {
            return false;
        }
        return isAssertOrFailMethod(extractSimpleMethodName(methodCall)) && node.equals(findLastExprIn(elist));
    }

    private static DetailAST findLastExprIn(DetailAST parent) {
        return collectExprChildren(parent).stream()
            .reduce((first, second) -> second)
            .orElse(null);
    }

    private static java.util.List<DetailAST> collectExprChildren(DetailAST parent) {
        java.util.List<DetailAST> exprs = new java.util.ArrayList<>();
        for (DetailAST child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == EXPR) {
                exprs.add(child);
            }
        }
        return exprs;
    }

    private static String extractSimpleMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot != null) {
            DetailAST last = dot.getLastChild();
            return last != null ? last.getText() : "";
        }
        DetailAST ident = methodCall.findFirstToken(IDENT);
        return ident != null ? ident.getText() : "";
    }

    private static boolean isAssertOrFailMethod(String name) {
        return name.startsWith("assert") || name.startsWith("fail") || name.startsWith("verify");
    }
}
