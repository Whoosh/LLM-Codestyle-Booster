package test;

// NOPMD this line has a suppression comment
// CHECKSTYLE:OFF another suppression
public class NoSuppressionInvalid {

    @SuppressWarnings("unchecked")
    public void withSuppressWarnings() {
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    public void withSpotBugsSuppression() {
    }
}
