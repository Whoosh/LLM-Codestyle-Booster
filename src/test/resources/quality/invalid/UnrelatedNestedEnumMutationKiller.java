package quality.invalid;

public class UnrelatedNestedEnumMutationKiller {

    private String name;

    public String getName() {
        return name;
    }

    // Enum with no OBJBLOCK at all should not crash
    // (Actually enums always have objblock, but this covers collectOwnNames line 76)

    // Enum that declares local variables and methods with names matching outer — still unrelated
    // because all IDENT refs inside are for its own declarations (isIdentReference line 163)
    enum Standalone {
        A, B;

        // 'name' here is a local parameter, not the outer field
        void process(String name) {
            System.out.println(name);
        }
    }
}
