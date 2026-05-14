package test;

// Verifies UnusedPrivateMembersCheck flags unused private RECORD and INTERFACE
// nested types in addition to classes/enums/methods/fields.
public class UnusedPrivateMembersRecordsInterfaces {

    private record UnusedPair(String a) { }

    private interface UnusedSpi {
        void run();
    }

    public void publicEntry() {
        // intentionally references nothing
    }
}
