package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstAnnotationUtil;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Enforces that {@code @SpringBootApplication} classes declare a {@code public static void main(String[])}.
 * The JVM bootstrap requires the entry point to be public — package-private, protected, or
 * non-static main methods cause Spring Boot to fail at startup with a launcher error.
 */
public class SpringBootMainVisibilityCheck extends AbstractCheck {

    /**
     * Violation message key — main method is not public.
     */
    static final String MSG_NOT_PUBLIC = "spring.boot.main.not.public";

    /**
     * Violation message key — main method is missing entirely.
     */
    static final String MSG_MISSING = "spring.boot.main.missing";

    /**
     * Violation message key — main method is not static.
     */
    static final String MSG_NOT_STATIC = "spring.boot.main.not.static";

    private static final String SPRING_BOOT_APPLICATION = "SpringBootApplication";
    private static final String MAIN_METHOD_NAME = "main";
    private static final int[] TOKENS = {CLASS_DEF};

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
    public void visitToken(DetailAST classDef) {
        if (!AstAnnotationUtil.hasAnnotationNamed(classDef, SPRING_BOOT_APPLICATION)) {
            return;
        }
        DetailAST classBody = classDef.findFirstToken(OBJBLOCK);
        if (classBody == null) {
            return;
        }
        DetailAST mainMethod = findMainMethod(classBody);
        DetailAST classIdent = classDef.findFirstToken(IDENT);
        String className = classIdent != null ? classIdent.getText() : "";
        if (mainMethod == null) {
            log(classDef.getLineNo(), classDef.getColumnNo(), MSG_MISSING, className);
            return;
        }
        if (!AstUtil.hasModifier(mainMethod, LITERAL_STATIC)) {
            log(mainMethod.getLineNo(), mainMethod.getColumnNo(), MSG_NOT_STATIC, className);
        }
        if (!AstUtil.hasModifier(mainMethod, LITERAL_PUBLIC)) {
            log(mainMethod.getLineNo(), mainMethod.getColumnNo(), MSG_NOT_PUBLIC, className);
        }
    }

    private static DetailAST findMainMethod(DetailAST classBody) {
        for (DetailAST child = classBody.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() != METHOD_DEF) {
                continue;
            }
            DetailAST ident = child.findFirstToken(IDENT);
            if (ident == null || !MAIN_METHOD_NAME.equals(ident.getText())) {
                continue;
            }
            if (hasStringArrayParameter(child)) {
                return child;
            }
        }
        return null;
    }

    private static boolean hasStringArrayParameter(DetailAST methodDef) {
        DetailAST params = methodDef.findFirstToken(PARAMETERS);
        if (params == null || params.getChildCount(PARAMETER_DEF) != 1) {
            return false;
        }
        DetailAST param = params.findFirstToken(PARAMETER_DEF);
        DetailAST type = param.findFirstToken(TYPE);
        return type != null && hasArrayOrVarargs(type, param) && containsStringIdent(type);
    }

    private static boolean hasArrayOrVarargs(DetailAST type, DetailAST param) {
        return type.findFirstToken(ARRAY_DECLARATOR) != null || param.findFirstToken(ELLIPSIS) != null;
    }

    private static boolean containsStringIdent(DetailAST node) {
        if (node.getType() == IDENT && "String".equals(node.getText())) {
            return true;
        }
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (containsStringIdent(child)) {
                return true;
            }
        }
        return false;
    }
}
