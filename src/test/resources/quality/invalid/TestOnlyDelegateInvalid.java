package test;

import java.util.List;
import java.util.Map;

public class TestOnlyDelegateInvalid {

    // Case 1: thin return-delegate to private method — classic pattern
    static Map<String, String> splitByPattern(String text) {
        return buildProblems(text);
    }

    // Case 2: thin void-delegate to private method
    static void runProcess(String input) {
        executeInternal(input);
    }

    // Case 3: delegate with parameter computation — still a thin delegate
    // (findMatches is also private, the whole method exists only for test access)
    static Map<String, String> splitWithMatches(String text, String pattern) {
        return buildProblems(findMatches(text, pattern));
    }

    // Case 4: public delegate to private — stronger signal than package-private
    public static String publicDelegate(String text) {
        return processInternal(text);
    }

    // Case 5: instance method delegating to private instance method
    String instanceDelegate(String input) {
        return doWork(input);
    }

    // Case 6: delegate that passes a subset of its params
    static String partialForward(String a, String b) {
        return processInternal(a);
    }

    private static Map<String, String> buildProblems(String text) {
        return Map.of();
    }

    private static String findMatches(String text, String pattern) {
        return text;
    }

    private static void executeInternal(String input) {
    }

    private static String processInternal(String text) {
        return text;
    }

    private String doWork(String input) {
        return input;
    }
}
