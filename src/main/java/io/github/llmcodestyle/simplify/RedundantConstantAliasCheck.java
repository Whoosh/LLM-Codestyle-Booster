package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Detects two same-class anti-patterns around {@code static final} fields:
 * <ol>
 *   <li><b>Redundant alias</b>: {@code static final T A = B;} where {@code B} is another
 *       sibling {@code static final} of compatible type. The alias adds nothing — call
 *       sites should reference {@code B} directly.</li>
 *   <li><b>Redundant Pattern.compile</b>: {@code static final Pattern X = Pattern.compile(R);}
 *       where another sibling {@code Pattern} field is already compiled from the same
 *       string value (either via the same constant name or via a literal that resolves
 *       to the same value). The second compile produces a duplicate {@link java.util.regex.Pattern}
 *       instance — call sites should reference the existing field.</li>
 * </ol>
 */
public class RedundantConstantAliasCheck extends AbstractCheck {

    /**
     * Violation message key for redundant aliases.
     */
    static final String MSG_ALIAS = "redundant.constant.alias";

    /**
     * Violation message key for redundant Pattern.compile of an already-compiled regex.
     */
    static final String MSG_PATTERN_DUP = "redundant.pattern.compile";

    private static final String PATTERN_TYPE = "Pattern";
    private static final String PATTERN_COMPILE = "compile";
    private static final int[] TOKENS = {CLASS_DEF, INTERFACE_DEF, ENUM_DEF, RECORD_DEF};

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
    public void visitToken(DetailAST typeDef) {
        DetailAST objBlock = typeDef.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return;
        }
        boolean insideInterface = typeDef.getType() == INTERFACE_DEF;
        Map<String, String> stringConstants = collectStringConstants(objBlock, insideInterface);
        Map<String, DetailAST> staticFinalFields = collectStaticFinalFields(objBlock, insideInterface);
        flagAliases(staticFinalFields);
        flagDuplicatePatterns(staticFinalFields, stringConstants);
    }

    private static Map<String, String> collectStringConstants(DetailAST objBlock, boolean insideInterface) {
        Map<String, String> result = new HashMap<>();
        for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() != VARIABLE_DEF || !isEffectivelyStaticFinal(child, insideInterface)) {
                continue;
            }
            if (!"String".equals(extractTypeName(child))) {
                continue;
            }
            String literal = stringLiteralInitializer(child);
            String name = fieldName(child);
            if (literal != null && name != null) {
                result.put(name, literal);
            }
        }
        return result;
    }

    private static Map<String, DetailAST> collectStaticFinalFields(DetailAST objBlock, boolean insideInterface) {
        Map<String, DetailAST> fields = new LinkedHashMap<>();
        for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() != VARIABLE_DEF || !isEffectivelyStaticFinal(child, insideInterface)) {
                continue;
            }
            String name = fieldName(child);
            if (name != null) {
                fields.put(name, child);
            }
        }
        return fields;
    }

    private void flagAliases(Map<String, DetailAST> fields) {
        for (Map.Entry<String, DetailAST> entry : fields.entrySet()) {
            DetailAST varDef = entry.getValue();
            String aliasOf = simpleIdentInitializer(varDef);
            if (aliasOf == null) {
                continue;
            }
            DetailAST target = fields.get(aliasOf);
            if (target != null && typesCompatible(extractTypeName(varDef), extractTypeName(target))) {
                log(varDef.getLineNo(), varDef.getColumnNo(), MSG_ALIAS, entry.getKey(), aliasOf);
            }
        }
    }

    private void flagDuplicatePatterns(Map<String, DetailAST> fields, Map<String, String> stringConstants) {
        Map<String, String> seenRegexValueToFieldName = new HashMap<>();
        for (Map.Entry<String, DetailAST> entry : fields.entrySet()) {
            DetailAST varDef = entry.getValue();
            if (!PATTERN_TYPE.equals(extractTypeName(varDef))) {
                continue;
            }
            String compiledValue = patternCompileValue(varDef, stringConstants);
            if (compiledValue == null) {
                continue;
            }
            String existing = seenRegexValueToFieldName.get(compiledValue);
            if (existing == null) {
                seenRegexValueToFieldName.put(compiledValue, entry.getKey());
            } else {
                log(varDef.getLineNo(), varDef.getColumnNo(), MSG_PATTERN_DUP, entry.getKey(), existing);
            }
        }
    }

    private static String simpleIdentInitializer(DetailAST varDef) {
        DetailAST assign = varDef.findFirstToken(ASSIGN);
        if (assign == null) {
            return null;
        }
        DetailAST expr = assign.findFirstToken(EXPR);
        if (expr == null || expr.getChildCount() != 1) {
            return null;
        }
        DetailAST first = expr.getFirstChild();
        return first != null && first.getType() == IDENT ? first.getText() : null;
    }

    private static String patternCompileValue(DetailAST varDef, Map<String, String> stringConstants) {
        DetailAST assign = varDef.findFirstToken(ASSIGN);
        if (assign == null) {
            return null;
        }
        DetailAST expr = assign.findFirstToken(EXPR);
        DetailAST methodCall = expr == null ? null : expr.findFirstToken(METHOD_CALL);
        if (methodCall == null || !isPatternCompileCall(methodCall)) {
            return null;
        }
        DetailAST elist = methodCall.findFirstToken(ELIST);
        if (elist == null || elist.getChildCount(EXPR) != 1) {
            return null;
        }
        DetailAST argExpr = elist.findFirstToken(EXPR);
        DetailAST argChild = argExpr == null ? null : argExpr.getFirstChild();
        if (argChild == null) {
            return null;
        }
        if (argChild.getType() == STRING_LITERAL) {
            return stripQuotes(argChild.getText());
        }
        if (argChild.getType() == IDENT) {
            return stringConstants.get(argChild.getText());
        }
        return null;
    }

    private static boolean isPatternCompileCall(DetailAST methodCall) {
        DetailAST dot = methodCall.getFirstChild();
        if (dot == null || dot.getType() != DOT) {
            return false;
        }
        DetailAST receiver = dot.getFirstChild();
        DetailAST method = receiver == null ? null : receiver.getNextSibling();
        return receiver != null
            && receiver.getType() == IDENT
            && PATTERN_TYPE.equals(receiver.getText())
            && method != null
            && method.getType() == IDENT
            && PATTERN_COMPILE.equals(method.getText());
    }

    private static String stringLiteralInitializer(DetailAST varDef) {
        DetailAST assign = varDef.findFirstToken(ASSIGN);
        if (assign == null) {
            return null;
        }
        DetailAST expr = assign.findFirstToken(EXPR);
        DetailAST first = expr == null ? null : expr.getFirstChild();
        return first != null && first.getType() == STRING_LITERAL ? stripQuotes(first.getText()) : null;
    }

    private static String stripQuotes(String literal) {
        if (literal == null || literal.length() < 2) {
            return "";
        }
        return literal.substring(1, literal.length() - 1);
    }

    private static String fieldName(DetailAST varDef) {
        DetailAST ident = varDef.findFirstToken(IDENT);
        return ident == null ? null : ident.getText();
    }

    private static String extractTypeName(DetailAST varDef) {
        DetailAST type = varDef.findFirstToken(TYPE);
        if (type == null) {
            return "";
        }
        DetailAST ident = type.findFirstToken(IDENT);
        return ident == null ? "" : ident.getText();
    }

    private static boolean typesCompatible(String left, String right) {
        return left != null && left.equals(right);
    }

    private static boolean isEffectivelyStaticFinal(DetailAST varDef, boolean insideInterface) {
        if (insideInterface) {
            return true;
        }
        DetailAST modifiers = varDef.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        boolean hasStatic = false;
        boolean hasFinal = false;
        for (DetailAST mod = modifiers.getFirstChild(); mod != null; mod = mod.getNextSibling()) {
            if (mod.getType() == LITERAL_STATIC) {
                hasStatic = true;
            } else if (mod.getType() == FINAL) {
                hasFinal = true;
            }
        }
        return hasStatic && hasFinal;
    }

}
