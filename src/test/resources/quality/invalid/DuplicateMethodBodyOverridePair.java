package quality.invalid;

import java.util.Objects;

public class DuplicateMethodBodyOverridePair {

    private Object data;

    // Non-override method with a body that's structurally the same as equals below
    public boolean same(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        DuplicateMethodBodyOverridePair that = (DuplicateMethodBodyOverridePair) other;
        return Objects.equals(data, that.data);
    }

    // Override method with the exact same body structure — should be SKIPPED by isOverride
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        DuplicateMethodBodyOverridePair that = (DuplicateMethodBodyOverridePair) other;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
