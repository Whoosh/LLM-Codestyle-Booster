package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstQueryUtil.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Shared heuristic for the four nested-type checks
 * ({@link io.github.llmcodestyle.quality.checks.UnrelatedNestedRecordCheck},
 * {@link io.github.llmcodestyle.quality.checks.UnrelatedNestedEnumCheck},
 * {@link io.github.llmcodestyle.quality.checks.UnrelatedNestedClassCheck},
 * {@link io.github.llmcodestyle.quality.checks.UnrelatedNestedInterfaceCheck}):
 * given a nested type, collect the names of fields and methods on the immediately enclosing
 * type, then walk the nested type's body for identifier references matching any of those
 * names. If none are found, the nested type is flagged as unrelated and should be extracted
 * to a top-level type (or a dedicated package for pojos / enums).
 *
 * <p>Subclasses supply the target token type ({@code RECORD_DEF}, {@code ENUM_DEF},
 * {@code CLASS_DEF}, or {@code INTERFACE_DEF}), the violation message key, and a way to
 * collect the nested type's own declared names so that internal self-references are not
 * mistaken for references to the enclosing type.
 */
public abstract class UnrelatedNestedTypeCheckBase extends AbstractCheck {

    private static final Set<Integer> DECL_NAME_TOKENS = Set.of(VARIABLE_DEF, METHOD_DEF, PARAMETER_DEF);
    private static final Set<Integer> DECLARATION_PARENT_TYPES = Set.of(
        VARIABLE_DEF,
        METHOD_DEF,
        PARAMETER_DEF,
        CLASS_DEF,
        INTERFACE_DEF,
        ENUM_DEF,
        ENUM_CONSTANT_DEF,
        RECORD_DEF,
        RECORD_COMPONENT_DEF,
        ANNOTATION,
        TYPE);

    private int[] cachedTokens;

    protected abstract int targetToken();

    protected abstract String messageKey();

    protected abstract void collectOwnDeclaredNames(DetailAST typeDef, Set<String> names);

    private int[] tokens() {
        if (cachedTokens == null) {
            cachedTokens = new int[] {targetToken()};
        }
        return cachedTokens;
    }

    @Override
    public final int[] getDefaultTokens() {
        return tokens().clone();
    }

    @Override
    public final int[] getAcceptableTokens() {
        return tokens().clone();
    }

    @Override
    public final int[] getRequiredTokens() {
        return tokens().clone();
    }

    @Override
    public final void visitToken(DetailAST typeDef) {
        DetailAST outerType = findEnclosingType(typeDef);
        if (outerType == null || referencesAnyOuterName(typeDef, collectOuterMemberNames(outerType), collectOwnNames(typeDef))) {
            return;
        }
        DetailAST ident = typeDef.findFirstToken(IDENT);
        log(typeDef.getLineNo(), typeDef.getColumnNo(), messageKey(), ident != null ? ident.getText() : "<anonymous>");
    }

    private Set<String> collectOwnNames(DetailAST typeDef) {
        Set<String> names = new HashSet<>();
        collectOwnDeclaredNames(typeDef, names);
        DetailAST objblock = typeDef.findFirstToken(OBJBLOCK);
        if (objblock != null) {
            collectDeclaredNames(objblock, names);
        }
        return names;
    }

    protected static void collectEnumConstantNames(DetailAST objblock, Set<String> names) {
        for (DetailAST child = objblock.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == ENUM_CONSTANT_DEF) {
                addIdentTo(child, names);
            }
        }
    }

    private static Set<String> collectOuterMemberNames(DetailAST outerType) {
        Set<String> names = new HashSet<>();
        DetailAST objblock = outerType.findFirstToken(OBJBLOCK);
        if (objblock != null) {
            addDirectFieldAndMethodNames(objblock, names);
        }
        collectRecordComponentNames(outerType, names);
        return names;
    }

    private static void addDirectFieldAndMethodNames(DetailAST objblock, Set<String> names) {
        for (DetailAST child = objblock.getFirstChild(); child != null; child = child.getNextSibling()) {
            int type = child.getType();
            if (type == VARIABLE_DEF || type == METHOD_DEF) {
                addIdentTo(child, names);
            }
        }
    }

    private static void collectDeclaredNames(DetailAST node, Set<String> names) {
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (DECL_NAME_TOKENS.contains(child.getType())) {
                addIdentTo(child, names);
            }
            collectDeclaredNames(child, names);
        }
    }

    private static boolean referencesAnyOuterName(DetailAST typeDef, Set<String> outerNames, Set<String> ownNames) {
        DetailAST objblock = typeDef.findFirstToken(OBJBLOCK);
        return objblock != null && scanForReferences(objblock, outerNames, ownNames);
    }

    private static boolean scanForReferences(DetailAST node, Set<String> outerNames, Set<String> ownNames) {
        // LITERAL_THIS is intentionally not treated as an outer reference: enum constants
        // and records are implicitly static, so `this` always refers to the nested type's
        // own instance, never to the enclosing type.
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (matchesOuterIdent(child, outerNames, ownNames) || scanForReferences(child, outerNames, ownNames)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesOuterIdent(DetailAST node, Set<String> outerNames, Set<String> ownNames) {
        if (!isIdentReference(node)) {
            return false;
        }
        String name = node.getText();
        return outerNames.contains(name) && !ownNames.contains(name);
    }

    private static boolean isIdentReference(DetailAST node) {
        if (node.getType() != IDENT) {
            return false;
        }
        DetailAST parent = node.getParent();
        if (parent == null) {
            return false;
        }
        return !DECLARATION_PARENT_TYPES.contains(parent.getType()) || !node.equals(parent.findFirstToken(IDENT));
    }
}
