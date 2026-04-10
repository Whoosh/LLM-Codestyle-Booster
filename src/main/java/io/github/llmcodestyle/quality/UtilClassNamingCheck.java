package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

/**
 * Enforces naming conventions for two adjacent kinds of static-only classes:
 *
 * <ul>
 *   <li><b>Utility classes</b>: a non-abstract class whose every {@code METHOD_DEF}
 *       carries the {@code static} modifier and has at least one {@code public}
 *       static method must end in {@code Util} or {@code Utils} (or be a
 *       constants holder, see below).</li>
 *   <li><b>Constants classes</b>: a class whose simple name ends in {@code Constants}
 *       must not declare any public method. Constants holders should expose only
 *       public static final fields.</li>
 * </ul>
 *
 * <p>Skipped: abstract classes, non-static nested classes, interfaces, enums,
 * records, and annotation types — none of which fit either category.
 *
 * <p>Constructors are not counted as "methods" for either rule, so a private
 * default constructor is fine.
 */
public class UtilClassNamingCheck extends AbstractCheck {

    /**
     * Violation key for the utility-naming rule.
     */
    static final String MSG_UTIL_NAME = "util.class.must.end.in.util";

    /**
     * Violation key for the Constants-no-public-method rule.
     */
    static final String MSG_CONSTANTS_PUBLIC_METHOD = "constants.class.public.method";

    private static final String CONSTANTS_SUFFIX = "Constants";
    private static final String UTIL_SUFFIX = "Util";
    private static final String UTILS_SUFFIX = "Utils";
    private static final int[] TOKENS = {CLASS_DEF};

    private int totalMethods;
    private int staticMethods;
    private int publicMethods;

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
        if (hasModifier(classDef, ABSTRACT) || isNonStaticNestedClass(classDef)) {
            return;
        }
        DetailAST nameIdent = classDef.findFirstToken(IDENT);
        if (nameIdent == null) {
            return;
        }
        DetailAST objBlock = classDef.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return;
        }
        countMethods(objBlock);
        String name = nameIdent.getText();
        if (name.endsWith(CONSTANTS_SUFFIX) && publicMethods > 0) {
            log(nameIdent.getLineNo(), nameIdent.getColumnNo(), MSG_CONSTANTS_PUBLIC_METHOD, name);
        }
        if (totalMethods > 0 && totalMethods == staticMethods && publicMethods > 0 && !isAcceptableUtilityName(name)) {
            log(nameIdent.getLineNo(), nameIdent.getColumnNo(), MSG_UTIL_NAME, name);
        }
    }

    private static boolean isNonStaticNestedClass(DetailAST classDef) {
        return isNestedType(classDef) && !hasModifier(classDef, LITERAL_STATIC);
    }

    private void countMethods(DetailAST objBlock) {
        totalMethods = 0;
        staticMethods = 0;
        publicMethods = 0;
        for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() != METHOD_DEF) {
                continue;
            }
            totalMethods++;
            if (hasModifier(child, LITERAL_STATIC)) {
                staticMethods++;
            }
            if (hasModifier(child, LITERAL_PUBLIC)) {
                publicMethods++;
            }
        }
    }

    private static boolean isAcceptableUtilityName(String name) {
        return name.endsWith(UTIL_SUFFIX) || name.endsWith(UTILS_SUFFIX) || name.endsWith(CONSTANTS_SUFFIX);
    }
}
