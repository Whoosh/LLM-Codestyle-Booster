package quality.invalid;

public class DuplicateMethodBodyAnnotatedPair {

    // Non-Override annotation should NOT cause the method to be skipped
    @Deprecated
    public void processAlpha(Object node) {
        Object child = firstChild(node);
        while (child != null) {
            inspect(child);
            child = nextSibling(child);
        }
    }

    // This is a duplicate of processAlpha, also with a non-Override annotation
    @SuppressWarnings("unused")
    public void processBeta(Object node) {
        Object child = firstChild(node);
        while (child != null) {
            inspect(child);
            child = nextSibling(child);
        }
    }

    // This overridden method should be SKIPPED
    @Override
    public String toString() {
        Object child = firstChild(this);
        while (child != null) {
            inspect(child);
            child = nextSibling(child);
        }
        return "";
    }

    private Object firstChild(Object n) { return n; }
    private Object nextSibling(Object n) { return n; }
    private void inspect(Object n) { }
}
