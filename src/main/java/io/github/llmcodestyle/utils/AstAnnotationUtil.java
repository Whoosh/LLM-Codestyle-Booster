package io.github.llmcodestyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

/**
 * Utility methods for checking annotations on AST definition nodes.
 */
public final class AstAnnotationUtil {

    private AstAnnotationUtil() {
    }

    /**
     * Returns {@code true} if {@code def} carries an annotation whose simple name equals {@code annotationName}.
     */
    public static boolean hasAnnotationNamed(DetailAST def, String annotationName) {
        DetailAST modifiers = def.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        DetailAST child = modifiers.getFirstChild();
        while (child != null) {
            if (child.getType() == ANNOTATION) {
                DetailAST identNode = child.findFirstToken(IDENT);
                if (identNode != null && annotationName.equals(identNode.getText())) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code def} carries any annotation whose simple name (or qualified last segment) is in {@code annotationNames}.
     */
    public static boolean hasAnyAnnotationNamed(DetailAST def, String... annotationNames) {
        DetailAST modifiers = def.findFirstToken(MODIFIERS);
        if (modifiers == null) {
            return false;
        }
        DetailAST child = modifiers.getFirstChild();
        while (child != null) {
            if (child.getType() == ANNOTATION && simpleNameMatches(child, annotationNames)) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    private static boolean simpleNameMatches(DetailAST annotation, String... annotationNames) {
        DetailAST identNode = annotation.findFirstToken(IDENT);
        if (identNode != null && nameInList(identNode.getText(), annotationNames)) {
            return true;
        }
        DetailAST dot = annotation.findFirstToken(DOT);
        DetailAST last = dot != null ? dot.getLastChild() : null;
        return last != null && last.getType() == IDENT && nameInList(last.getText(), annotationNames);
    }

    private static boolean nameInList(String name, String... candidates) {
        for (String candidate : candidates) {
            if (candidate.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
