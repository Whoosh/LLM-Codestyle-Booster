package quality.invalid;

import jakarta.annotation.Nullable;

import java.util.Objects;

public class MissingNullableParameterInvalid {

    // 1. Null-checked with return default -> needs @Nullable
    public static String fixKatex(String text) {
        if (text == null) {
            return "";
        }
        return text.toUpperCase();
    }

    // 2. Null-checked in ternary -> needs @Nullable
    public String format(String template) {
        return template == null ? "default" : template.trim();
    }

    // 3. Null-guarded with != null -> needs @Nullable
    public int process(String data) {
        if (data != null) {
            return data.length();
        }
        return 0;
    }

    // 4. Null literal passed to method without null handling
    public void caller() {
        transform(null);
    }

    private String transform(String input) {
        return input.toUpperCase();
    }

    // 5. Null-checked without @Nullable (used by callerToNullHandled)
    private String transform2(String input) {
        if (input == null) {
            return "fallback";
        }
        return input.toLowerCase();
    }

    // --- Should NOT flag ---

    // Already @Nullable -> no violation
    public String safe(@Nullable String text) {
        if (text == null) {
            return "";
        }
        return text;
    }

    // Null rejection (throw) -> no violation
    public String strict(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text required");
        }
        return text.toUpperCase();
    }

    // No null check -> no violation
    public String echo(String text) {
        return text;
    }

    // requireNonNull (no null comparison) -> no violation
    public String validated(String text) {
        Objects.requireNonNull(text);
        return text.trim();
    }

    // Null passed to method with @Nullable param -> no call-site violation
    public void callerToNullable() {
        safe(null);
    }

    // Null passed to method that handles null -> no call-site violation
    public void callerToNullHandled() {
        transform2(null);
    }
}
