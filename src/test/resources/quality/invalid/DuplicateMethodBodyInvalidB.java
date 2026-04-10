package quality.invalid;

public class DuplicateMethodBodyInvalidB {

    public int multiplyPair(int x, int y) {
        int total = x + y;
        total = total * 2;
        return total;
    }

    public int differentLiteral(int x, int y) {
        int total = x + y;
        total = total * 3;
        return total;
    }

    public void walkSibling(Object n) {
        Object child = firstChild(n);
        while (child != null) {
            inspect(child);
            child = nextSibling(child);
        }
    }

    private Object firstChild(Object n) {
        return n;
    }

    private Object nextSibling(Object n) {
        return n;
    }

    private void inspect(Object c) {
    }
}
