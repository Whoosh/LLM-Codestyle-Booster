package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.utils.AstUtil;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Flags private fields, methods, and inner types that are never referenced in the same source file. Uses name-based reference counting.
 */
public class UnusedPrivateMembersCheck extends AbstractCheck {

    /**
     * Violation message key for unused private members.
     */
    static final String MSG_KEY = "unused.private.member";
    private static final int[] TOKENS = {CLASS_DEF};

    private static final Set<String> EXCLUDED_NAMES = Set.of("serialVersionUID");

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
        if (AstUtil.isNestedType(ast)) {
            return;
        }
        checkFile(ast);
    }

    private void checkFile(DetailAST outerClass) {
        Map<String, Integer> privates = new HashMap<>();
        Set<DetailAST> declarationIdents = new HashSet<>();

        collectAllPrivateDeclarations(outerClass, privates, declarationIdents);

        Set<String> allRefs = new HashSet<>();
        collectReferences(outerClass, allRefs, declarationIdents);

        for (Map.Entry<String, Integer> entry : privates.entrySet()) {
            String name = entry.getKey();
            if (!allRefs.contains(name) && !EXCLUDED_NAMES.contains(name)) {
                log(entry.getValue(), 0, MSG_KEY, name);
            }
        }
    }

    private static void collectAllPrivateDeclarations(DetailAST classDef, Map<String, Integer> privates, Set<DetailAST> declarationIdents) {
        DetailAST objBlock = classDef.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return;
        }
        collectPrivateDeclarations(objBlock, privates, declarationIdents);
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            int type = child.getType();
            if (type == CLASS_DEF || type == ENUM_DEF || type == INTERFACE_DEF || type == RECORD_DEF) {
                collectAllPrivateDeclarations(child, privates, declarationIdents);
            }
            child = child.getNextSibling();
        }
    }

    private static void collectPrivateDeclarations(DetailAST objBlock, Map<String, Integer> privates, Set<DetailAST> declarationIdents) {
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            int type = child.getType();
            if ((type == VARIABLE_DEF || type == METHOD_DEF || type == CLASS_DEF || type == ENUM_DEF) && isPrivateNonAnnotated(child)) {
                DetailAST ident = child.findFirstToken(IDENT);
                if (ident != null) {
                    privates.putIfAbsent(ident.getText(), ident.getLineNo());
                    declarationIdents.add(ident);
                }
            }
            child = child.getNextSibling();
        }
    }

    private static boolean isPrivateNonAnnotated(DetailAST member) {
        DetailAST modifiers = member.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        boolean isPrivate = false;
        DetailAST mod = modifiers.getFirstChild();
        while (mod != null) {
            if (mod.getType() == LITERAL_PRIVATE) {
                isPrivate = true;
            }
            if (mod.getType() == ANNOTATION) {
                DetailAST annotIdent = mod.findFirstToken(IDENT);
                if (annotIdent != null && "Override".equals(annotIdent.getText())) {
                    return false;
                }
            }
            mod = mod.getNextSibling();
        }
        return isPrivate;
    }

    private static void collectReferences(DetailAST node, Set<String> refs, Set<DetailAST> excludeNodes) {
        if (node == null) {
            return;
        }
        if (node.getType() == IDENT && !excludeNodes.contains(node)) {
            refs.add(node.getText());
        }
        DetailAST child = node.getFirstChild();
        while (child != null) {
            collectReferences(child, refs, excludeNodes);
            child = child.getNextSibling();
        }
    }
}
