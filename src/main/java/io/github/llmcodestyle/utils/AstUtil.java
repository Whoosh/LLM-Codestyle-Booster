package io.github.llmcodestyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Shared AST traversal utilities: package names, type nesting, modifiers, and line ranges.
 */
public final class AstUtil {

    private AstUtil() {
    }

    /**
     * Extracts the fully-qualified package name string from a {@code PACKAGE_DEF} node.
     */
    public static String extractPackageName(DetailAST packageDef) {
        StringBuilder sb = new StringBuilder();
        DetailAST child = packageDef.getFirstChild();
        while (child != null) {
            if (child.getType() == DOT || child.getType() == IDENT) {
                buildDottedName(child, sb);
                break;
            }
            child = child.getNextSibling();
        }
        return sb.toString();
    }

    /**
     * Recursively appends dotted segments of a qualified name into {@code sb}.
     */
    public static void buildDottedName(DetailAST node, StringBuilder sb) {
        if (node == null) {
            return;
        }
        if (node.getType() == IDENT) {
            if (!sb.isEmpty()) {
                sb.append('.');
            }
            sb.append(node.getText());
        } else if (node.getType() == DOT) {
            buildDottedName(node.getFirstChild(), sb);
            buildDottedName(node.getLastChild(), sb);
        }
    }

    /**
     * Returns {@code true} if {@code classDef} is lexically nested inside another class, interface, or enum.
     */
    public static boolean isInnerClass(DetailAST classDef) {
        DetailAST parent = classDef.getParent();
        while (parent != null) {
            int type = parent.getType();
            if (type == CLASS_DEF || type == INTERFACE_DEF || type == ENUM_DEF) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code typeDef} is lexically nested inside another class, interface, enum, or record.
     */
    public static boolean isNestedType(DetailAST typeDef) {
        DetailAST parent = typeDef.getParent();
        while (parent != null) {
            int type = parent.getType();
            if (type == CLASS_DEF || type == INTERFACE_DEF || type == ENUM_DEF || type == RECORD_DEF) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Returns the nesting depth of {@code node} relative to enclosing type declarations. A top-level type has depth 0.
     */
    public static int typeNestingDepth(DetailAST node) {
        int depth = 0;
        DetailAST parent = node.getParent();
        while (parent != null) {
            int type = parent.getType();
            if (type == CLASS_DEF || type == INTERFACE_DEF || type == ENUM_DEF || type == RECORD_DEF) {
                depth++;
            }
            parent = parent.getParent();
        }
        return depth;
    }

    /**
     * Returns {@code true} if the definition node {@code def} has a MODIFIERS child containing {@code modifierType}.
     */
    public static boolean hasModifier(DetailAST def, int modifierType) {
        DetailAST modifiers = def.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        DetailAST child = modifiers.getFirstChild();
        while (child != null) {
            if (child.getType() == modifierType) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    /**
     * Returns the highest line number in the entire subtree rooted at {@code ast}.
     */
    public static int findLastLine(DetailAST ast) {
        int last = ast.getLineNo();
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            int childLast = findLastLine(child);
            if (childLast > last) {
                last = childLast;
            }
            child = child.getNextSibling();
        }
        return last;
    }

    /**
     * Counts the number of chained method calls rooted at {@code methodCall} (e.g. {@code a.b().c().d()} has chain length 3).
     */
    public static int countMethodChain(DetailAST methodCall) {
        int count = 1;
        DetailAST dot = methodCall.findFirstToken(DOT);
        while (dot != null) {
            DetailAST child = dot.getFirstChild();
            if (child != null && child.getType() == METHOD_CALL) {
                count++;
                dot = child.findFirstToken(DOT);
            } else {
                break;
            }
        }
        return count;
    }
}
