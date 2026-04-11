package io.github.llmcodestyle.utils;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.pojos.InstanceScope;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstQueryUtil.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Helpers that decide whether a method body depends on instance state of its enclosing type.
 * Shared by {@link io.github.llmcodestyle.quality.MethodMayBeStaticCheck may-be-static detection}
 * and {@link io.github.llmcodestyle.quality.DuplicateMethodBodyCheck duplicate-extractable-to-util detection}.
 *
 * <p>The heuristic intentionally errs on the side of under-flagging:
 * <ul>
 *   <li>Any unqualified call whose name is not a locally declared method is assumed to be an
 *       inherited instance call (e.g. {@code log(...)} from a framework base class).</li>
 *   <li>Bare {@code IDENT} references that match an instance field name are treated as reads
 *       of that field, even if a local variable would actually shadow it.</li>
 * </ul>
 */
public final class AstInstanceStateUtil {

    private AstInstanceStateUtil() {
    }

    /**
     * Walks the direct children of {@code typeDef}'s {@code OBJBLOCK} to collect its instance
     * scope. Does not descend into nested types.
     */
    public static InstanceScope collectScope(DetailAST typeDef) {
        Set<String> fields = new HashSet<>();
        Set<String> instanceMethods = new HashSet<>();
        Set<String> declaredMethods = new HashSet<>();
        DetailAST objBlock = typeDef.findFirstToken(OBJBLOCK);
        if (objBlock != null) {
            for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
                classifyChild(child, fields, instanceMethods, declaredMethods);
            }
        }
        if (typeDef.getType() == RECORD_DEF) {
            collectRecordComponentNames(typeDef, fields);
        }
        return new InstanceScope(fields, instanceMethods, declaredMethods);
    }

    private static void classifyChild(DetailAST child, Set<String> fields, Set<String> instanceMethods, Set<String> declaredMethods) {
        int type = child.getType();
        if (type == VARIABLE_DEF && !hasModifier(child, LITERAL_STATIC)) {
            addIdentTo(child, fields);
        } else if (type == METHOD_DEF) {
            addIdentTo(child, declaredMethods);
            if (!hasModifier(child, LITERAL_STATIC)) {
                addIdentTo(child, instanceMethods);
            }
        }
    }

    /**
     * Returns {@code true} if the subtree rooted at {@code node} references instance state of
     * its enclosing type as described by {@code scope}.
     */
    public static boolean referencesInstanceState(DetailAST node, InstanceScope scope) {
        int type = node.getType();
        if (type == LITERAL_THIS || type == LITERAL_SUPER
            || type == METHOD_CALL && isInstanceCall(node, scope)
            || type == IDENT && scope.instanceFields().contains(node.getText())) {
            return true;
        }
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (referencesInstanceState(child, scope)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInstanceCall(DetailAST methodCall, InstanceScope scope) {
        DetailAST first = methodCall.getFirstChild();
        if (first == null || first.getType() != IDENT) {
            return false;
        }
        String name = first.getText();
        return scope.instanceMethods().contains(name) || !scope.declaredMethods().contains(name);
    }
}
