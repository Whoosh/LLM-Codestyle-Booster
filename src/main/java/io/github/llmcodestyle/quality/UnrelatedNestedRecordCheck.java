package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static io.github.llmcodestyle.utils.AstUtil.*;

import java.util.Set;

/**
 * Detects {@code record} declarations nested inside another class, interface, or enum that
 * do not reference any field or method of the enclosing type. Such records are pure data
 * carriers and should live in a dedicated {@code pojos} package for reuse and discoverability.
 *
 * <p>Heuristic details live in {@link UnrelatedNestedTypeCheckBase}.
 */
public class UnrelatedNestedRecordCheck extends UnrelatedNestedTypeCheckBase {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "unrelated.nested.record";

    @Override
    protected int targetToken() {
        return RECORD_DEF;
    }

    @Override
    protected String messageKey() {
        return MSG_KEY;
    }

    @Override
    protected void collectOwnDeclaredNames(DetailAST recordDef, Set<String> names) {
        collectRecordComponentNames(recordDef, names);
    }
}
