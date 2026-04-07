package test;

public class ForbidAssertKeywordInvalid {

    void simpleAssert(Object obj) {
        assert obj != null;
    }

    void assertWithMessage(String s) {
        assert s.length() > 0 : "must not be empty";
    }
}
