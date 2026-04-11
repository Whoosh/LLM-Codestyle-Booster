package quality.valid;

// Valid: nested records in a non-pojos package are out of scope for this check
// (handled by UnrelatedNestedRecordCheck). The enclosing class itself must not be flagged.
public class TopLevelRecordInPojosPackageNestedOk {

    private final int counter = 0;

    public int counter() {
        return counter;
    }

    record Nested(int a, int b) {
    }

    static class InnerHolder {

        record Deep(String name) {
        }
    }
}
