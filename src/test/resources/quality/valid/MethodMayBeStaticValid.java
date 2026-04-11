package quality.valid;

import static java.util.Arrays.asList;

public class MethodMayBeStaticValid {

    private int counter;

    private final String name = "x";

    // Uses this directly → keep instance.
    private int snapshot() {
        return this.counter;
    }

    // Uses an instance field by name → keep instance.
    private void inc() {
        counter++;
    }

    // Calls another instance method without explicit receiver → keep instance.
    private int incrementAndRead() {
        inc();
        return counter;
    }

    // Uses super → keep instance.
    private String describe() {
        return super.toString();
    }

    // Already static — never visited.
    private static int constant() {
        return 1;
    }

    // Annotated as override — skipped even though body is parameterless.
    @Override
    public String toString() {
        return name;
    }

    // Lambda body captures `this` via name reference.
    private java.util.function.IntSupplier supplier() {
        return () -> counter * 2;
    }

    // Uses an instance field shadowed by a local — pessimistic skip.
    private int shadowed(int counter) {
        int local = counter + 1;
        return local;
    }

    // Default method in nested interface — see static-allowed test fixture.
    public interface Inner {

        default int answer() {
            return 1;
        }
    }

    // Non-static nested class is skipped entirely.
    class NonStaticNested {

        private int doStuff(int x) {
            return x + 1;
        }
    }

    // Unqualified call to a static-imported helper from java.util.Arrays. The check
    // cannot tell static-imports apart from inherited instance methods, so it
    // pessimistically refuses to mark this method static.
    private int useStaticImport(Object o) {
        return asList(o).size();
    }

    // Public stateless method in a non-final class → must NOT be flagged, because a
    // subclass could override it and promoting to static would be a breaking change.
    public int publicOverridableHelper(int x, int y) {
        return x * y + 1;
    }

    // Package-private stateless method in a non-final class → same story, skipped.
    int packagePrivateOverridableHelper(int x) {
        return x + 1;
    }
}
