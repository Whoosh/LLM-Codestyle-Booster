package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstAnnotationUtil.*;
import static io.github.llmcodestyle.utils.AstQueryUtil.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Detects {@code private} non-static methods whose body never references {@code this},
 * {@code super}, or any instance member of the enclosing type. Such methods can — and
 * should — be marked {@code static}: it documents intent, lets callers reason about
 * the method without an instance, and improves JIT specialisation.
 *
 * <p>The check stays conservative on purpose:
 * <ul>
 *   <li>Only {@code private} methods are flagged. Promoting a wider-scoped method to
 *       {@code static} can break overrides and binary compatibility.</li>
 *   <li>{@code @Override}, {@code abstract}, and {@code native} methods are skipped.</li>
 *   <li>Methods inside non-static nested classes are skipped — they may implicitly
 *       depend on outer-instance state without naming it.</li>
 *   <li>Any reference to a name that matches an instance field or instance method of
 *       the enclosing type is treated as a potential dependency, even if a local would
 *       shadow it. Better to under-flag than to break compilation.</li>
 *   <li>An unqualified method call whose name does not match any locally declared
 *       method is treated as a potential inherited instance call (e.g. {@code log(...)}
 *       inherited from a superclass). The candidate is skipped to avoid breaking
 *       compilation for subclasses of framework types.</li>
 *   <li>Record components count as instance fields.</li>
 * </ul>
 */
public class MethodMayBeStaticCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "method.may.be.static";
    private static final int[] TOKENS = {CLASS_DEF, RECORD_DEF, ENUM_DEF, INTERFACE_DEF};

    private final Set<String> instanceFields = new HashSet<>();
    private final Set<String> instanceMethods = new HashSet<>();
    private final Set<String> declaredMethods = new HashSet<>();

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
        if (isNonStaticNestedClass(typeDef)) {
            return;
        }
        DetailAST objBlock = typeDef.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return;
        }
        instanceFields.clear();
        instanceMethods.clear();
        declaredMethods.clear();
        collectScope(typeDef, objBlock);
        for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == METHOD_DEF && isCandidate(child)) {
                analyzeMethod(child);
            }
        }
    }

    private void analyzeMethod(DetailAST methodDef) {
        DetailAST slist = methodDef.findFirstToken(SLIST);
        if (slist == null || referencesInstanceState(slist)) {
            return;
        }
        DetailAST ident = methodDef.findFirstToken(IDENT);
        log(methodDef.getLineNo(), methodDef.getColumnNo(), MSG_KEY, ident != null ? ident.getText() : "?");
    }

    private static boolean isNonStaticNestedClass(DetailAST typeDef) {
        // Records, enums, interfaces, and nested classes inside them are implicitly static
        // — only a CLASS_DEF can be a non-static inner class.
        return isNestedType(typeDef) && typeDef.getType() == CLASS_DEF && !hasModifier(typeDef, LITERAL_STATIC);
    }

    private static boolean isCandidate(DetailAST methodDef) {
        if (!hasModifier(methodDef, LITERAL_PRIVATE) || hasModifier(methodDef, LITERAL_STATIC) || hasModifier(methodDef, ABSTRACT) || hasModifier(methodDef, LITERAL_NATIVE)) {
            return false;
        }
        return !hasAnnotationNamed(methodDef, "Override");
    }

    private void collectScope(DetailAST typeDef, DetailAST objBlock) {
        for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
            int type = child.getType();
            if (type == VARIABLE_DEF && !hasModifier(child, LITERAL_STATIC)) {
                addIdentTo(child, instanceFields);
            } else if (type == METHOD_DEF) {
                addIdentTo(child, declaredMethods);
                if (!hasModifier(child, LITERAL_STATIC)) {
                    addIdentTo(child, instanceMethods);
                }
            }
        }
        if (typeDef.getType() == RECORD_DEF) {
            collectRecordComponentNames(typeDef, instanceFields);
        }
    }

    private boolean referencesInstanceState(DetailAST node) {
        int type = node.getType();
        if (type == LITERAL_THIS || type == LITERAL_SUPER || type == METHOD_CALL && isInstanceCall(node) || type == IDENT && instanceFields.contains(node.getText())) {
            return true;
        }
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (referencesInstanceState(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInstanceCall(DetailAST methodCall) {
        DetailAST first = methodCall.getFirstChild();
        if (first == null || first.getType() != IDENT) {
            // Qualified call (DOT) — receiver is checked separately as IDENT/LITERAL_THIS.
            return false;
        }
        String name = first.getText();
        // Unqualified calls fall in three buckets:
        //   1. locally declared instance method → reads `this` implicitly
        //   2. locally declared static method   → safe
        //   3. anything else (inherited instance method like log(), or a static import)
        //      → we cannot prove it is safe, so be pessimistic.
        return instanceMethods.contains(name) || !declaredMethods.contains(name);
    }
}
