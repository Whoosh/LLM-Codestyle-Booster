package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.Set;

/**
 * Detects {@code interface} declarations nested inside another type that do not reference any
 * field or method of the enclosing type. Such interfaces should be extracted to their own
 * top-level file for reuse and discoverability.
 *
 * <p>Heuristic details live in {@link UnrelatedNestedTypeCheckBase}.
 */
public class UnrelatedNestedInterfaceCheck extends UnrelatedNestedTypeCheckBase {

    static final String MSG_KEY = "unrelated.nested.interface";

    @Override
    protected int targetToken() {
        return INTERFACE_DEF;
    }

    @Override
    protected String messageKey() {
        return MSG_KEY;
    }

    @Override
    protected void collectOwnDeclaredNames(DetailAST interfaceDef, Set<String> names) {
        // All names are inside OBJBLOCK, which the base class collects automatically.
    }
}
