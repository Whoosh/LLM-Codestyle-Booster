package simplify.invalid;

public class IfReturnBooleanLiteralSemiPath {

    private int value;

    // The RCURLY after the if body is the if's sibling before the return
    // This ensures followingReturnInBlock traverses RCURLY and SEMI siblings
    public boolean withRcurly() {
        if (value > 0) {
            return true;
        } // RCURLY is part of if's SLIST
        return false;
    }
}
