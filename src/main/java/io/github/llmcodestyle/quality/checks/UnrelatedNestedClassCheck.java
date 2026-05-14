package io.github.llmcodestyle.quality.checks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.quality.UnrelatedNestedTypeCheckBase;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.Set;

/**
 * Detects {@code class} declarations nested inside another type that do not reference any
 * field or method of the enclosing type. Such classes should be extracted to their own
 * top-level file for reuse and discoverability.
 *
 * <p>Heuristic details live in {@link UnrelatedNestedTypeCheckBase}.
 */
public class UnrelatedNestedClassCheck extends UnrelatedNestedTypeCheckBase {

    static final String MSG_KEY = "unrelated.nested.class";

    @Override
    protected int targetToken() {
        return CLASS_DEF;
    }

    @Override
    protected String messageKey() {
        return MSG_KEY;
    }

    @Override
    protected void collectOwnDeclaredNames(DetailAST classDef, Set<String> names) {
        // All names are inside OBJBLOCK, which the base class collects automatically.
    }
}
