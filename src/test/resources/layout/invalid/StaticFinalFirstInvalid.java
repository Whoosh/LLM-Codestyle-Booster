package test;

public class StaticFinalFirstInvalid {

    private int instanceField = 0;

    static final String CONSTANT_AFTER_INSTANCE = "value";

    private String anotherInstance = "x";

    static final int ANOTHER_CONST = 42;
}

// Records also have ordering constraints — static final after a canonical constructor must be flagged.
record StaticFinalFirstInvalidRecord(int value) {

    StaticFinalFirstInvalidRecord(int value) {
        this.value = value;
    }

    static final String RECORD_CONST_AFTER_CTOR = "late";
}

