package quality.valid;

import jakarta.annotation.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class ExplicitNullReturnValid {

    // @Nullable annotated — documented null contract, no violation
    @Nullable
    public String findOptional() {
        if (Math.random() > 0.5) {
            return "found";
        }
        return null;
    }

    // Returns non-null value — no violation
    public String getName() {
        return "John";
    }

    // Returns call to non-@Nullable method — no violation
    public String getGreeting() {
        return getName();
    }

    // void method — no violation
    public void doSomething() {
        return;
    }

    // Returns Optional instead of null — no violation
    public Optional<String> findSafe() {
        return Optional.empty();
    }

    // Lambda with block body returning null — does not flag outer method
    public Supplier<String> lazyBlock() {
        return () -> {
            return null;
        };
    }

    // Null is passed as argument, not returned — no violation
    public String format(String template) {
        return template.formatted((Object) null);
    }

    // External method call (obj.foo()) — not tracked, no false positive
    public String fromExternal(ExplicitNullReturnValid other) {
        return other.findOptional();
    }
}
