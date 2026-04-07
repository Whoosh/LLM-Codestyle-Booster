package test;

import org.junit.jupiter.api.Test;

public class TestMethodNameInvalid {

    @Test
    void should_return_valid_result() {
    }

    @Test
    void given_input_when_called_then_success() {
    }

    @Test
    void test_with_underscore() {
    }

    // Regression: fully-qualified annotation must also be detected
    @org.junit.jupiter.api.Test
    void fully_qualified_annotation_underscore() {
    }
}
