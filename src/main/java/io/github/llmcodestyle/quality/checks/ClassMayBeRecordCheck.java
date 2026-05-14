package io.github.llmcodestyle.quality.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

import java.util.Set;

/**
 * Flags {@code final} classes that look like pure data carriers — only private instance fields,
 * no instance methods other than constructors and the standard {@code equals}/{@code hashCode}/
 * {@code toString} — and suggests converting them to {@code record} declarations.
 *
 * <p>Disqualifying conditions:
 * <ul>
 *   <li>class is not {@code final} (records are implicitly final);</li>
 *   <li>class has an {@code extends} clause (records cannot extend classes);</li>
 *   <li>class has no instance fields (utility class, not a data carrier);</li>
 *   <li>any instance field is not {@code private};</li>
 *   <li>class has any instance method other than constructors, {@code equals},
 *       {@code hashCode}, or {@code toString}.</li>
 * </ul>
 *
 * <p>Static fields, static methods, nested types, and initialiser blocks are allowed —
 * records support all of these.
 */
public class ClassMayBeRecordCheck extends AbstractCheck {

    static final String MSG_KEY = "class.may.be.record";

    private static final int[] TOKENS = {CLASS_DEF};
    private static final Set<String> STANDARD_METHODS = Set.of("equals", "hashCode", "toString");

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
        if (!hasModifier(classDef, FINAL) || hasModifier(classDef, ABSTRACT) || classDef.findFirstToken(EXTENDS_CLAUSE) != null) {
            return;
        }
        DetailAST objblock = classDef.findFirstToken(OBJBLOCK);
        if (objblock == null) {
            return;
        }
        int instanceFields = countAndValidateFields(objblock);
        if (instanceFields <= 0 || hasDisqualifyingMethods(objblock)) {
            return;
        }
        log(classDef.getLineNo(), classDef.getColumnNo(), MSG_KEY, classDef.findFirstToken(IDENT).getText(), instanceFields);
    }

    /**
     * Returns the number of instance fields, or {@code -1} if any instance field is not private.
     */
    private static int countAndValidateFields(DetailAST objblock) {
        int count = 0;
        for (DetailAST child = objblock.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == VARIABLE_DEF && !hasModifier(child, LITERAL_STATIC)) {
                if (!hasModifier(child, LITERAL_PRIVATE)) {
                    return -1;
                }
                count++;
            }
        }
        return count;
    }

    private static boolean hasDisqualifyingMethods(DetailAST objblock) {
        for (DetailAST child = objblock.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == METHOD_DEF && !hasModifier(child, LITERAL_STATIC)) {
                DetailAST ident = child.findFirstToken(IDENT);
                if (ident == null || !STANDARD_METHODS.contains(ident.getText())) {
                    return true;
                }
            }
        }
        return false;
    }
}
