package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Flags {@code static final} fields whose initializer is a {@code +} expression that can be collapsed into a single literal constant.
 */
public class CollapsibleConstantConcatenationCheck extends AbstractCheck {

    /**
     * Violation message key for scalar field initializers.
     */
    static final String MSG_KEY = "collapsible.constant.concatenation";

    /**
     * Violation message key for array element concatenations.
     */
    static final String MSG_ARRAY = "collapsible.array.element.concatenation";

    /**
     * Violation message key for consecutive constant/literal runs in method bodies.
     */
    static final String MSG_RUN = "collapsible.constant.run";
    private static final int[] TOKENS = {CLASS_DEF, INTERFACE_DEF, ENUM_DEF, RECORD_DEF};
    private static final Set<Integer> METHOD_LIKE_DEFS = Set.of(METHOD_DEF, CTOR_DEF, COMPACT_CTOR_DEF);
    private static final Set<Integer> SINGLE_LITERAL_TOKENS = Set.of(STRING_LITERAL, NUM_INT, NUM_LONG, NUM_FLOAT, NUM_DOUBLE, CHAR_LITERAL);

    private static final int MIN_METHOD_RUN_LENGTH = 2;

    /**
     * Maximum line length matching the project's LineLength rule. The check skips suggestions
     * whose merged literal would exceed this width — joining would just create a different
     * (LineLength) violation.
     */
    private static final int MAX_LINE_LENGTH = 180;

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
        DetailAST objBlock = ast.findFirstToken(OBJBLOCK);
        if (objBlock == null) {
            return;
        }
        checkObjBlock(objBlock);
    }

    private void checkObjBlock(DetailAST objBlock) {
        boolean insideInterface = objBlock.getParent() != null && objBlock.getParent().getType() == INTERFACE_DEF;

        Set<String> literalConstants = collectLiteralConstants(objBlock, insideInterface);

        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == VARIABLE_DEF && isEffectivelyStaticFinal(child, insideInterface)) {
                checkFieldInitializer(child, literalConstants);
            }
            child = child.getNextSibling();
        }

        checkMethodBodies(objBlock, literalConstants);
    }

    private static Set<String> collectLiteralConstants(DetailAST objBlock, boolean insideInterface) {
        Set<String> result = new HashSet<>();
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == VARIABLE_DEF && isEffectivelyStaticFinal(child, insideInterface)) {
                addIfLiteralConstant(child, result);
            }
            child = child.getNextSibling();
        }
        return result;
    }

    private static void addIfLiteralConstant(DetailAST varDef, Set<String> result) {
        DetailAST assign = varDef.findFirstToken(ASSIGN);
        if (assign == null) {
            return;
        }
        DetailAST expr = assign.findFirstToken(EXPR);
        if (expr == null || !isSingleLiteral(expr.getFirstChild())) {
            return;
        }
        DetailAST ident = varDef.findFirstToken(IDENT);
        if (ident != null) {
            result.add(ident.getText());
        }
    }

    private void checkFieldInitializer(DetailAST varDef, Set<String> literalConstants) {
        DetailAST assign = varDef.findFirstToken(ASSIGN);
        if (assign == null) {
            return;
        }
        DetailAST directArrayInit = assign.findFirstToken(ARRAY_INIT);
        if (directArrayInit != null) {
            checkArrayInit(varDef, directArrayInit, literalConstants);
            return;
        }
        DetailAST expr = assign.findFirstToken(EXPR);
        if (expr == null) {
            return;
        }
        DetailAST exprChild = expr.getFirstChild();
        if (exprChild == null) {
            return;
        }
        if (exprChild.getType() == PLUS) {
            checkScalarPlus(varDef, exprChild, literalConstants);
            return;
        }
        DetailAST arrayInit = findArrayInit(exprChild);
        if (arrayInit != null) {
            checkArrayInit(varDef, arrayInit, literalConstants);
        }
    }

    private void checkScalarPlus(DetailAST varDef, DetailAST plus, Set<String> literalConstants) {
        if (allLeavesCollapsible(plus, literalConstants) && mergedLiteralWouldFitOnLine(varDef, plus)) {
            DetailAST ident = varDef.findFirstToken(IDENT);
            log(varDef.getLineNo(), varDef.getColumnNo(), MSG_KEY, ident != null ? ident.getText() : "?", countLeaves(plus));
        }
    }

    private boolean mergedLiteralWouldFitOnLine(DetailAST varDef, DetailAST plus) {
        int contentLength = computeMergedStringContentLength(plus);
        return contentLength < 0 || declarationPrefixLength(varDef) + 2 + contentLength + 1 <= MAX_LINE_LENGTH;
    }

    private int declarationPrefixLength(DetailAST varDef) {
        int eqIdx = getLines()[varDef.getLineNo() - 1].indexOf('=');
        return eqIdx < 0 ? varDef.getColumnNo() : eqIdx + 2;
    }

    private static int computeMergedStringContentLength(DetailAST node) {
        if (node == null) {
            return 0;
        }
        int type = node.getType();
        if (type == STRING_LITERAL) {
            return Math.max(0, node.getText().length() - 2);
        }
        if (type == EXPR) {
            return computeMergedStringContentLength(node.getFirstChild());
        }
        if (type != PLUS) {
            return -1;
        }
        DetailAST left = node.getFirstChild();
        int leftLen = computeMergedStringContentLength(left);
        int rightLen = computeMergedStringContentLength(left == null ? null : left.getNextSibling());
        return leftLen < 0 || rightLen < 0 ? -1 : leftLen + rightLen;
    }

    private void checkArrayInit(DetailAST varDef, DetailAST arrayInit, Set<String> literalConstants) {
        DetailAST ident = varDef.findFirstToken(IDENT);
        String fieldName = ident != null ? ident.getText() : "?";
        DetailAST child = arrayInit.getFirstChild();
        while (child != null) {
            if (child.getType() == EXPR) {
                DetailAST inner = child.getFirstChild();
                if (inner != null && inner.getType() == PLUS && allLeavesCollapsible(inner, literalConstants)) {
                    log(inner.getLineNo(), inner.getColumnNo(), MSG_ARRAY, fieldName, countLeaves(inner));
                }
            }
            child = child.getNextSibling();
        }
    }

    private static DetailAST findArrayInit(DetailAST node) {
        if (node == null) {
            return null;
        }
        if (node.getType() == ARRAY_INIT) {
            return node;
        }
        if (node.getType() == LITERAL_NEW) {
            return node.findFirstToken(ARRAY_INIT);
        }
        return null;
    }

    private static boolean allLeavesCollapsible(DetailAST node, Set<String> literalConstants) {
        if (node == null) {
            return false;
        }
        if (node.getType() == PLUS) {
            DetailAST left = node.getFirstChild();
            return allLeavesCollapsible(left, literalConstants) && allLeavesCollapsible(left.getNextSibling(), literalConstants);
        }
        if (node.getType() == EXPR) {
            return allLeavesCollapsible(node.getFirstChild(), literalConstants);
        }
        return isSingleLiteral(node) || node.getType() == IDENT && literalConstants.contains(node.getText());
    }

    private static int countLeaves(DetailAST node) {
        if (node == null) {
            return 0;
        }
        if (node.getType() == PLUS) {
            DetailAST left = node.getFirstChild();
            return countLeaves(left) + countLeaves(left != null ? left.getNextSibling() : null);
        }
        if (node.getType() == EXPR) {
            return countLeaves(node.getFirstChild());
        }
        return 1;
    }

    private void checkMethodBodies(DetailAST objBlock, Set<String> literalConstants) {
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (METHOD_LIKE_DEFS.contains(child.getType())) {
                DetailAST slist = child.findFirstToken(SLIST);
                if (slist != null) {
                    scanForCollapsibleRuns(slist, literalConstants);
                }
            }
            child = child.getNextSibling();
        }
    }

    private void scanForCollapsibleRuns(DetailAST node, Set<String> literalConstants) {
        if (node == null) {
            return;
        }
        if (node.getType() == PLUS && !(node.getParent() != null && node.getParent().getType() == PLUS)) {
            List<DetailAST> leaves = new ArrayList<>();
            flattenPlus(node, leaves);
            checkForCollapsibleRun(leaves, literalConstants);
            return;
        }
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForCollapsibleRuns(child, literalConstants);
            child = child.getNextSibling();
        }
    }

    private static void flattenPlus(DetailAST node, List<DetailAST> leaves) {
        if (node.getType() == PLUS) {
            DetailAST left = node.getFirstChild();
            DetailAST right = left != null ? left.getNextSibling() : null;
            if (left != null) {
                flattenPlus(left, leaves);
            }
            if (right != null) {
                flattenPlus(right, leaves);
            }
        } else if (node.getType() == EXPR) {
            flattenPlus(node.getFirstChild(), leaves);
        } else {
            leaves.add(node);
        }
    }

    private void checkForCollapsibleRun(List<DetailAST> leaves, Set<String> literalConstants) {
        int runStart = -1;
        int runLength = 0;
        for (int i = 0; i < leaves.size(); i++) {
            DetailAST leaf = leaves.get(i);
            if (isSingleLiteral(leaf) || leaf.getType() == IDENT && literalConstants.contains(leaf.getText())) {
                if (runLength == 0) {
                    runStart = i;
                }
                runLength++;
            } else {
                if (runLength >= MIN_METHOD_RUN_LENGTH) {
                    log(leaves.get(runStart).getLineNo(), leaves.get(runStart).getColumnNo(), MSG_RUN, runLength);
                }
                runLength = 0;
            }
        }
        if (runLength >= MIN_METHOD_RUN_LENGTH) {
            log(leaves.get(runStart).getLineNo(), leaves.get(runStart).getColumnNo(), MSG_RUN, runLength);
        }
    }

    private static boolean isSingleLiteral(DetailAST node) {
        return node != null && SINGLE_LITERAL_TOKENS.contains(node.getType());
    }

    /**
     * Interface fields are implicitly static final even without explicit modifiers.
     */
    private static boolean isEffectivelyStaticFinal(DetailAST variableDef, boolean insideInterface) {
        if (insideInterface) {
            return true;
        }
        DetailAST modifiers = variableDef.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        boolean hasStatic = false;
        boolean hasFinal = false;
        DetailAST mod = modifiers.getFirstChild();
        while (mod != null) {
            if (mod.getType() == LITERAL_STATIC) {
                hasStatic = true;
            } else if (mod.getType() == FINAL) {
                hasFinal = true;
            }
            mod = mod.getNextSibling();
        }
        return hasStatic && hasFinal;
    }
}
