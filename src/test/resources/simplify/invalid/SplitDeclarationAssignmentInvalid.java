package simplify.invalid;

public class SplitDeclarationAssignmentInvalid {

    public int simpleCase() {
        int x;
        x = 5;
        return x;
    }

    public String stringDecl() {
        String name;
        name = "hello";
        return name;
    }

    public int withOtherStmtsBefore() {
        int unrelated = 0;
        unrelated++;
        int x;
        x = 42;
        return x + unrelated;
    }

    public int withOtherStmtsAfter() {
        int x;
        x = 5;
        x++;
        return x;
    }

    public int adjacentMultiple() {
        int a;
        int b;
        a = 1;
        b = 2;
        return a + b;
    }

    public int useOfOtherVarBetween() {
        int x = 0;
        int y;
        x++;
        y = x + 1;
        return y;
    }

    public int variableDeclaredBetween() {
        int x;
        int y = computeY();
        x = y + 1;
        return x;
    }

    private int computeY() {
        return 0;
    }
}
