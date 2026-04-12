package quality.valid;

public final class MethodMayBeStaticMutKiller {

    // Record with method that references a component — should NOT be flagged as may-be-static
    // because the component is instance state.
    // AstInstanceStateUtil.collectScope: if RECORD_DEF is negated, components won't be collected
    // and this method would be falsely flagged.
    record Person(String name, int age) {

        String greeting() {
            return "Hello, " + name;
        }

        int nextAge() {
            return age + 1;
        }
    }

    // Class with instance method calling another instance method — not flagged
    private int value;

    private int computeDouble() {
        return value * 2;
    }
}
