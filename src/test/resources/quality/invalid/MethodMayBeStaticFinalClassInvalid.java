package quality.invalid;

// Mirrors the real-world PhysicsPromptBuilder pattern: a public final class where a
// stateless public method must be flagged as may-be-static, because a final class
// cannot be subclassed and no subclass can override the method.
public final class MethodMayBeStaticFinalClassInvalid {

    private final String systemPromptText;

    public MethodMayBeStaticFinalClassInvalid() {
        this.systemPromptText = "prompt";
    }

    // Reads instance field → keep instance.
    public String systemPrompt() {
        return systemPromptText;
    }

    // Uses only its parameter and a method call on that parameter (receiver is a
    // local, not `this`) → may be static.
    public String buildUserMessage(QuizRequest request) {
        return "theme=" + request.theme() + ", grade=" + request.grade();
    }

    // Package-private stateless method in a final class → may be static.
    String formatGreeting(String name, String template) {
        return "Hello, " + name.trim() + "! " + template;
    }

    // Final method with stateless body in a final class → may be static (final-method
    // branch of the override-safety check).
    final int doubleOf(int x) {
        return x * 2;
    }

    record QuizRequest(String theme, String grade) {
    }
}
