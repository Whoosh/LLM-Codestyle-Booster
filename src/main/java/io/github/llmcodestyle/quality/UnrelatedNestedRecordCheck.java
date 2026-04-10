package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Detects {@code record} declarations nested inside another class, interface, or enum that
 * do not reference any field or method of the enclosing type. Such records are pure data
 * carriers and should live in a dedicated {@code pojos} package for reuse and discoverability.
 *
 * <p>Heuristic: collects names of fields and methods declared directly in the immediately
 * enclosing type, then walks the nested record body looking for identifier references with
 * matching names. If none are found, the record is flagged.
 */
public class UnrelatedNestedRecordCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "unrelated.nested.record";
    private static final int[] TOKENS = {RECORD_DEF};
    private static final Set<Integer> DECLARATION_PARENT_TYPES = Set.of(
        VARIABLE_DEF,
        METHOD_DEF,
        PARAMETER_DEF,
        CLASS_DEF,
        INTERFACE_DEF,
        ENUM_DEF,
        RECORD_DEF,
        RECORD_COMPONENT_DEF,
        ANNOTATION,
        TYPE);

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
    public void visitToken(DetailAST recordDef) {
        DetailAST outerType = findEnclosingType(recordDef);
        if (outerType == null) {
            return;
        }
        if (referencesAnyOuterName(recordDef, collectOuterMemberNames(outerType), collectRecordOwnNames(recordDef))) {
            return;
        }
        DetailAST recordIdent = recordDef.findFirstToken(IDENT);
        log(recordDef.getLineNo(), recordDef.getColumnNo(), MSG_KEY, recordIdent != null ? recordIdent.getText() : "<anonymous>");
    }

    private static DetailAST findEnclosingType(DetailAST recordDef) {
        DetailAST parent = recordDef.getParent();
        while (parent != null) {
            int type = parent.getType();
            if (type == CLASS_DEF || type == INTERFACE_DEF || type == ENUM_DEF || type == RECORD_DEF) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private static Set<String> collectOuterMemberNames(DetailAST outerType) {
        Set<String> names = new HashSet<>();
        DetailAST objblock = outerType.findFirstToken(OBJBLOCK);
        if (objblock != null) {
            addDirectFieldAndMethodNames(objblock, names);
        }
        AstUtil.collectRecordComponentNames(outerType, names);
        return names;
    }

    private static Set<String> collectRecordOwnNames(DetailAST recordDef) {
        Set<String> names = new HashSet<>();
        AstUtil.collectRecordComponentNames(recordDef, names);
        DetailAST objblock = recordDef.findFirstToken(OBJBLOCK);
        if (objblock != null) {
            collectDeclaredNames(objblock, names);
        }
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

    private static void addIdentTo(DetailAST def, Set<String> names) {
        DetailAST ident = def.findFirstToken(IDENT);
        if (ident != null) {
            names.add(ident.getText());
        }
    }

    private static void collectDeclaredNames(DetailAST node, Set<String> names) {
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            int type = child.getType();
            if (type == VARIABLE_DEF || type == METHOD_DEF || type == PARAMETER_DEF) {
                addIdentTo(child, names);
            }
            collectDeclaredNames(child, names);
        }
    }

    private static boolean referencesAnyOuterName(DetailAST recordDef, Set<String> outerNames, Set<String> recordOwnNames) {
        DetailAST objblock = recordDef.findFirstToken(OBJBLOCK);
        return objblock != null && scanForReferences(objblock, outerNames, recordOwnNames);
    }

    private static boolean scanForReferences(DetailAST node, Set<String> outerNames, Set<String> recordOwnNames) {
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            // LITERAL_THIS is intentionally not treated as an outer reference: records are
            // implicitly static, so `this` always refers to the record's own instance.
            if (matchesOuterIdent(child, outerNames, recordOwnNames)) {
                return true;
            }
            if (scanForReferences(child, outerNames, recordOwnNames)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesOuterIdent(DetailAST node, Set<String> outerNames, Set<String> recordOwnNames) {
        if (!isIdentReference(node)) {
            return false;
        }
        String name = node.getText();
        return outerNames.contains(name) && !recordOwnNames.contains(name);
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
