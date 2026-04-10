package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.pojos.RegexConstantOccurrence;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;
import static io.github.llmcodestyle.utils.AstQueryUtil.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Detects duplicate regex constants across classes within a module.
 *
 * <p>When the same regex pattern appears as a {@code static final} constant
 * in multiple classes or records, the check flags the second (and subsequent)
 * occurrences, suggesting extraction to a shared utility class.
 *
 * <p>Detection covers:
 * <ul>
 *   <li>{@code static final Pattern} fields (always treated as regex)</li>
 *   <li>{@code static final String} fields whose value contains regex
 *       escape sequences ({@code \d}, {@code \w}, {@code \s}, etc.)</li>
 * </ul>
 */
public class DuplicateRegexConstantCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "duplicate.regex.constant";
    private static final int[] TOKENS = {VARIABLE_DEF};

    /**
     * Matches regex character-class escapes in Java string literal text.
     * Requires the escape sequence NOT to be followed by an alphanumeric char,
     * which filters out false positives like {@code "C:\\data"}.
     */
    private static final Pattern REGEX_INDICATOR = Pattern.compile("\\\\\\\\[dDwWsS](?![a-zA-Z0-9])");

    private static final String PATTERN_TYPE = "Pattern";
    private static final String UNKNOWN = "<unknown>";

    private final Map<String, RegexConstantOccurrence> seenRegex = new HashMap<>();

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
        if (!isStaticFinal(ast)) {
            return;
        }

        String regexValue = extractRegexValue(ast, extractTypeName(ast));
        if (regexValue == null) {
            return;
        }

        String constName = extractConstName(ast);
        String className = extractEnclosingClassName(ast);

        RegexConstantOccurrence prev = seenRegex.get(regexValue);
        if (prev != null) {
            log(ast, MSG_KEY, constName, prev.constName(), prev.className());
        } else {
            seenRegex.put(regexValue, new RegexConstantOccurrence(className, constName));
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        seenRegex.clear();
    }

    private static boolean isStaticFinal(DetailAST variableDef) {
        return hasModifier(variableDef, LITERAL_STATIC) && hasModifier(variableDef, FINAL);
    }

    private static String extractRegexValue(DetailAST variableDef, String typeName) {
        if (PATTERN_TYPE.equals(typeName)) {
            return extractPatternCompileArg(variableDef);
        }
        if ("String".equals(typeName)) {
            String literal = extractStringLiteral(variableDef);
            if (literal != null && REGEX_INDICATOR.matcher(literal).find()) {
                return literal;
            }
        }
        return null;
    }

    private static String extractStringLiteral(DetailAST variableDef) {
        String literal = findFirstTextInChain(variableDef, ASSIGN, EXPR, STRING_LITERAL);
        return literal.isEmpty() ? null : literal;
    }

    private static String extractPatternCompileArg(DetailAST variableDef) {
        DetailAST assign = variableDef.findFirstToken(ASSIGN);
        DetailAST expr = assign != null ? assign.findFirstToken(EXPR) : null;
        DetailAST methodCall = expr != null ? expr.findFirstToken(METHOD_CALL) : null;
        if (methodCall == null || !isPatternCompileCall(methodCall)) {
            return null;
        }
        String firstArg = findFirstTextInChain(methodCall, ELIST, EXPR, STRING_LITERAL);
        return firstArg.isEmpty() ? null : firstArg;
    }

    private static boolean isPatternCompileCall(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(DOT);
        if (dot == null) {
            return false;
        }
        DetailAST target = dot.getFirstChild();
        DetailAST method = dot.getLastChild();
        return target != null && PATTERN_TYPE.equals(target.getText()) && method != null && "compile".equals(method.getText());
    }

    private static String extractConstName(DetailAST variableDef) {
        DetailAST ident = variableDef.findFirstToken(IDENT);
        return ident != null ? ident.getText() : UNKNOWN;
    }

    private static String extractEnclosingClassName(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (TYPE_DECL_TOKENS.contains(parent.getType())) {
                DetailAST ident = parent.findFirstToken(IDENT);
                if (ident != null) {
                    return ident.getText();
                }
            }
            parent = parent.getParent();
        }
        return UNKNOWN;
    }

}
