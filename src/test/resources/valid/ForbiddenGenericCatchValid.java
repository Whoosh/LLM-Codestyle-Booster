package test;

import java.io.IOException;

public class ForbiddenGenericCatchValid {

    public void catchSpecificException() {
        try {
            doSomething();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void catchMultiSpecific() {
        try {
            doSomething();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    public void catchError() {
        try {
            doSomething();
        } catch (StackOverflowError ex) {
            ex.printStackTrace();
        }
    }

    private void doSomething() throws Exception {
    }
}
