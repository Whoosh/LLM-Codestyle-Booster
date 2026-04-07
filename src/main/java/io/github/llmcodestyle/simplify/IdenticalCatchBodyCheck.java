package io.github.llmcodestyle.simplify;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Detects multiple catch clauses in a single try statement with identical bodies.
 * Suggests merging into a multi-catch: {@code catch (A | B e)}.
 */
public class IdenticalCatchBodyCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    static final String MSG_KEY = "identical.catch.body";

    @Override
    public int[] getDefaultTokens() {
        return new int[] {LITERAL_TRY};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {LITERAL_TRY};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {LITERAL_TRY};
    }

    @Override
    public void visitToken(DetailAST tryAst) {
        for (List<CatchEntry> group : collectCatchEntries(tryAst).stream()
            .collect(Collectors.groupingBy(CatchEntry::fp))
            .values()) {
            if (group.size() >= 2) {
                for (CatchEntry entry : group) {
                    log(entry.ast(), MSG_KEY);
                }
            }
        }
    }

    private static List<CatchEntry> collectCatchEntries(DetailAST tryAst) {
        List<CatchEntry> entries = new ArrayList<>();
        for (DetailAST child = tryAst.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() != LITERAL_CATCH) {
                continue;
            }
            DetailAST slist = child.findFirstToken(SLIST);
            if (slist != null) {
                entries.add(new CatchEntry(child, fingerprint(slist, extractCaughtVarName(child))));
            }
        }
        return entries;
    }

    private record CatchEntry(DetailAST ast, String fp) {
    }

    private static String extractCaughtVarName(DetailAST catchAst) {
        DetailAST paramDef = catchAst.findFirstToken(PARAMETER_DEF);
        if (paramDef == null) {
            return "";
        }
        DetailAST ident = paramDef.findFirstToken(IDENT);
        return ident != null ? ident.getText() : "";
    }

    private static String fingerprint(DetailAST node, String exVarName) {
        StringBuilder sb = new StringBuilder();
        buildFingerprint(node, exVarName, sb);
        return sb.toString();
    }

    private static void buildFingerprint(DetailAST node, String exVarName, StringBuilder sb) {
        sb.append(node.getType());
        if (node.getType() == IDENT) {
            String text = node.getText();
            sb.append(exVarName.equals(text) ? ":$EX" : ":" + text);
        } else if (node.getType() == STRING_LITERAL || node.getType() == NUM_INT || node.getType() == NUM_LONG) {
            sb.append(':').append(node.getText());
        }
        sb.append('(');
        for (DetailAST child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            buildFingerprint(child, exVarName, sb);
        }
        sb.append(')');
    }
}
