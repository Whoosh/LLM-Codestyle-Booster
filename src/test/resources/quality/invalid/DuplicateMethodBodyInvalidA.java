package quality.invalid;

public class DuplicateMethodBodyInvalidA {

    public void walkTypeNode(Object typeNode) {
        Object child = firstChild(typeNode);
        while (child != null) {
            inspect(child);
            child = nextSibling(child);
        }
    }

    public void walkBorNode(Object bor) {
        Object child = firstChild(bor);
        while (child != null) {
            inspect(child);
            child = nextSibling(child);
        }
    }

    public int sumTwo(int a, int b) {
        int total = a + b;
        total = total * 2;
        return total;
    }

    private Object firstChild(Object n) {
        return n;
    }

    private Object nextSibling(Object n) {
        return n;
    }

    private void inspect(Object n) {
    }
}
