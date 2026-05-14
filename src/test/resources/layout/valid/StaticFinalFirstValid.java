package test;

public class StaticFinalFirstValid {

    static final String FIRST_CONSTANT = "a";
    static final int SECOND_CONSTANT = 1;

    private int instanceField = 0;
    private String anotherField = "x";

    public StaticFinalFirstValid() {
    }

    public void method() {
    }

    // Nested record with proper ordering: static final declared BEFORE the canonical
    // constructor — no violation. Made nested (was top-level) to avoid
    // TopLevelRecordInPojosPackageCheck firing in the ping-pong matrix.
    record StaticFinalFirstValidRecord(int value) {

        static final String RECORD_PREFIX = "ok";

        StaticFinalFirstValidRecord(int value) {
            this.value = value;
        }
    }
}

