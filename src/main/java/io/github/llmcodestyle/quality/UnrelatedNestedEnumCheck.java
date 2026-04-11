package io.github.llmcodestyle.quality;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.Set;

/**
 * Detects {@code enum} declarations nested inside another class, interface, enum, or record
 * that do not reference any field or method of the enclosing type. Such enums are freestanding
 * value sets and should live in a dedicated {@code enums} package for reuse and discoverability.
 *
 * <p>Heuristic details live in {@link UnrelatedNestedTypeCheckBase}. Enum constants are added
 * to the nested type's own-names set so that constants sharing a simple name with an outer
 * field do not falsely count as references to the enclosing type.
 */
public class UnrelatedNestedEnumCheck extends UnrelatedNestedTypeCheckBase {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "unrelated.nested.enum";

    @Override
    protected int targetToken() {
        return ENUM_DEF;
    }

    @Override
    protected String messageKey() {
        return MSG_KEY;
    }

    @Override
    protected void collectOwnDeclaredNames(DetailAST enumDef, Set<String> names) {
        DetailAST objblock = enumDef.findFirstToken(OBJBLOCK);
        if (objblock != null) {
            collectEnumConstantNames(objblock, names);
        }
    }
}
