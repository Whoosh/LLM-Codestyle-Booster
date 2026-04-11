package quality.valid;

// Valid: nested enums in a non-enums package are out of scope for this check
// (handled by UnrelatedNestedEnumCheck). The enclosing class itself must not be flagged.
public class TopLevelEnumInEnumsPackageNestedOk {

    private final int counter = 0;

    public int counter() {
        return counter;
    }

    enum NestedStatus {
        OPEN, CLOSED
    }

    static class InnerHolder {

        enum Phase {
            INIT, READY, DONE
        }
    }
}
