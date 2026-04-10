package io.github.llmcodestyle.layout;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import io.github.llmcodestyle.pojos.ImportInfo;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Requires static imports to use wildcard form unless there is a name collision.
 *
 * <p>Flags {@code import static com.foo.Bar.X} when no other static import in the
 * file imports a member named {@code X} from a different class. When a collision
 * exists (same member name from different classes), explicit imports are allowed
 * to avoid ambiguity.
 */
public class StaticStarImportCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "static.star.import";
    private static final int[] TOKENS = {STATIC_IMPORT};

    private final List<ImportInfo> explicitImports = new ArrayList<>();
    private final Map<String, List<String>> memberToParents = new HashMap<>();

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
    public void beginTree(DetailAST rootAST) {
        explicitImports.clear();
        memberToParents.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST dot = ast.findFirstToken(DOT);
        if (dot == null) {
            return;
        }
        DetailAST lastChild = dot.getLastChild();
        if (lastChild == null || lastChild.getType() == STAR) {
            return;
        }
        String memberName = lastChild.getText();
        String parentClass = extractParentClass(dot);
        explicitImports.add(new ImportInfo(ast, memberName, parentClass));
        memberToParents.computeIfAbsent(memberName, k -> new ArrayList<>()).add(parentClass);
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        for (ImportInfo info : explicitImports) {
            List<String> parents = memberToParents.get(info.memberName());
            if (parents != null && hasCollision(parents)) {
                continue;
            }
            log(info.ast(), MSG_KEY);
        }
        explicitImports.clear();
        memberToParents.clear();
    }

    private static boolean hasCollision(List<String> parents) {
        if (parents.size() < 2) {
            return false;
        }
        String first = parents.getFirst();
        for (int i = 1; i < parents.size(); i++) {
            if (!first.equals(parents.get(i))) {
                return true;
            }
        }
        return false;
    }

    private static String extractParentClass(DetailAST dot) {
        DetailAST parentDot = dot.getFirstChild();
        if (parentDot == null) {
            return "";
        }
        if (parentDot.getType() == DOT) {
            DetailAST classIdent = parentDot.getLastChild();
            return classIdent != null ? classIdent.getText() : "";
        }
        return parentDot.getText();
    }

}
