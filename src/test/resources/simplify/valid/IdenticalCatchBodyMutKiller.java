package com.example;

public class IdenticalCatchBodyMutKiller {

    // Two catches that call different methods — should NOT be identical
    // If buildFingerprint's IDENT check is negated, method name differences would be lost
    // and both catches would produce the same fingerprint, causing a false positive
    void differentMethodCalls(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            handleNumber(e);
        } catch (IllegalArgumentException e) {
            handleArgument(e);
        }
    }

    private void handleNumber(Exception e) { }
    private void handleArgument(Exception e) { }
}
