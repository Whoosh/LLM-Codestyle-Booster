package quality.invalid;

public class UnrelatedNestedEnumMutKiller2 {

    // Outer field named "name" — same as the enum constant below
    private String name;

    public String getName() {
        return name;
    }

    // This enum has a constant called NAME that matches the outer field "name" in a case-insensitive
    // way, but the actual IDENT references inside the enum are to the constant NAME, not the field.
    // The enum should be flagged as unrelated because it doesn't actually reference the outer field.
    // If collectEnumConstantNames is broken (objblock != null negated), the constant "NAME" won't be
    // added to own-names, so references to "NAME" inside the enum might not be filtered properly.
    // Actually we need the constant to have the EXACT same name as the outer field.
    enum Status {
        name, age;  // lowercase enum constants matching outer field name

        // References to 'name' inside this enum should refer to the constant, not outer field
        @Override
        public String toString() {
            return name.name();
        }
    }
}
