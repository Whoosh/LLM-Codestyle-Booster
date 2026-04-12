package test;

import org.junit.jupiter.api.Test;

public class LongTestLiteralFieldInTestMethod {

    @Test
    void testWithLocalClassField() {
        // A field initializer inside a local class within a @Test method
        // isInsideTestMethod returns true, but isFieldInitializer must also return true
        // to make shouldExempt return true
        class LocalHelper {
            private final String longField = "This is a very long field initializer inside a local class in a test method that should be exempt";
        }
        new LocalHelper();
    }
}
