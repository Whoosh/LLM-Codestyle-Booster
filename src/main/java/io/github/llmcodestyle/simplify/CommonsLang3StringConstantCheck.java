package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.Map;

/**
 * Detects {@code static final String} constants whose value duplicates one of the
 * well-known {@link org.apache.commons.lang3.StringUtils} constants. Replacing them
 * with the library equivalent (and adding a static import) keeps every project using
 * the same name for the same value, which makes search and review easier.
 *
 * <p>Recognized equivalents (Apache Commons Lang3 3.x):
 * <ul>
 *   <li>{@code ""} → {@code StringUtils.EMPTY}</li>
 *   <li>{@code " "} → {@code StringUtils.SPACE}</li>
 *   <li>{@code "\n"} → {@code StringUtils.LF}</li>
 *   <li>{@code "\r"} → {@code StringUtils.CR}</li>
 * </ul>
 *
 * <p>The check only inspects declarations of the form
 * {@code [public|private|protected] static final String NAME = "literal";}.
 * Inline literal usages (e.g. {@code s.split(" ")}) are intentionally not flagged
 * — that would produce too much noise for code that does not yet depend on
 * commons-lang3. Once you have introduced a {@code StringUtils.SPACE} reference,
 * the rest can be migrated by hand or via a follow-up sweep.
 */
public class CommonsLang3StringConstantCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "commons.lang3.string.constant";

    private static final Map<String, String> EQUIVALENTS = Map.of("\"\"", "EMPTY", "\" \"", "SPACE", "\"\\n\"", "LF", "\"\\r\"", "CR");
    private static final int[] TOKENS = {VARIABLE_DEF};

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
    public void visitToken(DetailAST varDef) {
        if (!isStaticFinalStringField(varDef)) {
            return;
        }
        String literal = stringLiteralInitText(varDef);
        if (literal == null) {
            return;
        }
        String equivalent = EQUIVALENTS.get(literal);
        if (equivalent == null) {
            return;
        }
        DetailAST nameIdent = varDef.findFirstToken(IDENT);
        if (nameIdent != null) {
            log(nameIdent.getLineNo(), nameIdent.getColumnNo(), MSG_KEY, nameIdent.getText(), equivalent);
        }
    }

    private static boolean isStaticFinalStringField(DetailAST varDef) {
        DetailAST parent = varDef.getParent();
        if (parent == null || parent.getType() != OBJBLOCK || !AstUtil.hasModifier(varDef, LITERAL_STATIC) || !AstUtil.hasModifier(varDef, FINAL)) {
            return false;
        }
        return "String".equals(AstUtil.extractTypeName(varDef));
    }

    private static String stringLiteralInitText(DetailAST varDef) {
        DetailAST assign = varDef.findFirstToken(ASSIGN);
        if (assign == null) {
            return null;
        }
        DetailAST expr = assign.findFirstToken(EXPR);
        if (expr == null) {
            return null;
        }
        DetailAST first = expr.getFirstChild();
        return first != null && first.getType() == STRING_LITERAL ? first.getText() : null;
    }
}
