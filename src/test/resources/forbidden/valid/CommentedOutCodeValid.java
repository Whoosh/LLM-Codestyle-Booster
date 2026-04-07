package test;

public class CommentedOutCodeValid {

    // Regular comment explaining algorithm
    // TODO: refactor this later
    // NOTE: this handles edge cases for Unicode
    // FIXME: performance issue under load

    // Section separator
    // -----------------------------------

    // Single commented-code line (under threshold of 2)
//    String unused = "x";

    public void method() {
        // explain what happens next
        // another line of explanation
        System.out.println("active code");
    }
}
