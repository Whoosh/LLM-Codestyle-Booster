package test;

public class ForbiddenGenericCatchInvalid {

    public void catchException() {
        try {
            doSomething();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void catchThrowable() {
        try {
            doSomething();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void catchRuntimeException() {
        try {
            doSomething();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    public void catchMultiWithGeneric() {
        try {
            doSomething();
        } catch (IllegalArgumentException | Exception ex) {
            ex.printStackTrace();
        }
    }

    // Regression: fully-qualified catch types
    public void catchFullyQualifiedException() {
        try {
            doSomething();
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        }
    }

    public void catchFullyQualifiedThrowable() {
        try {
            doSomething();
        } catch (java.lang.Throwable t) {
            t.printStackTrace();
        }
    }

    private void doSomething() throws Exception {
    }
}
