package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.CLASS_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.CTOR_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.ENUM_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.FINAL;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.IDENT;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.INTERFACE_DEF;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.LITERAL_STATIC;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.OBJBLOCK;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.VARIABLE_DEF;

/** Ensures {@code static final} fields are declared before instance fields and constructors. */
public class StaticFinalFirstCheck extends AbstractCheck {

    /** Violation message key. */
    static final String MSG_KEY = "static.final.after.instance";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{CLASS_DEF, INTERFACE_DEF, ENUM_DEF};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[]{CLASS_DEF, INTERFACE_DEF, ENUM_DEF};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]{CLASS_DEF, INTERFACE_DEF, ENUM_DEF};
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST objBlock = ast.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return;
        }
        checkFieldOrder(objBlock);
    }

    private void checkFieldOrder(DetailAST objBlock) {
        boolean seenInstanceFieldOrCtor = false;
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == VARIABLE_DEF) {
                if (isStaticFinal(child)) {
                    if (seenInstanceFieldOrCtor) {
                        DetailAST ident = child.findFirstToken(IDENT);
                        log(child.getLineNo(), child.getColumnNo(), MSG_KEY, ident != null ? ident.getText() : "?");
                    }
                } else if (!isStatic(child)) {
                    seenInstanceFieldOrCtor = true;
                }
            } else if (child.getType() == CTOR_DEF) {
                seenInstanceFieldOrCtor = true;
            }
            child = child.getNextSibling();
        }
    }

    private static boolean isStaticFinal(DetailAST variableDef) {
        return AstUtil.hasModifier(variableDef, LITERAL_STATIC) && AstUtil.hasModifier(variableDef, FINAL);
    }

    private static boolean isStatic(DetailAST variableDef) {
        return AstUtil.hasModifier(variableDef, LITERAL_STATIC);
    }
}
