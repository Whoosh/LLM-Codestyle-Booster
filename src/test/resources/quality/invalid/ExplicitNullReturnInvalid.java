package quality.invalid;

import jakarta.annotation.Nullable;

import java.util.List;

public class ExplicitNullReturnInvalid {

    private String cached;

    // 1. Direct null return → violation
    public String findName() {
        return null;
    }

    // 2. Delegation to @Nullable method → violation
    public String getName() {
        return findNullable();
    }

    // 3. this.nullableMethod() delegation → violation
    public String getNameQualified() {
        return this.findNullable();
    }

    // 4. Null in one branch of if → violation on that branch
    public String lookup(String key) {
        if (key.isEmpty()) {
            return null;
        }
        return key.toUpperCase();
    }

    // @Nullable annotated → no violation (properly documented null contract)
    @Nullable
    private String findNullable() {
        if (Math.random() > 0.5) {
            return "found";
        }
        return null;
    }

    // Returns non-null — no violation
    public String safe() {
        return "always";
    }

    // Returns call to non-@Nullable method — no violation
    public String delegateToSafe() {
        return safe();
    }

    // Uses instance state + returns null → still a violation (stateful doesn't exempt)
    public String getCached() {
        if (cached != null) {
            return cached;
        }
        return null;
    }
}
