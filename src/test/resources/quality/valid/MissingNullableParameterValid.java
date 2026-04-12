package quality.valid;

import jakarta.annotation.Nullable;

import java.util.Objects;

public class MissingNullableParameterValid {

    // @Nullable on null-checked param -> no violation
    @Nullable
    public static String fixKatex(@Nullable String text) {
        if (text == null) {
            return null;
        }
        return text.toUpperCase();
    }

    // Null rejection (if-throw) -> no violation
    public String validate(String text) {
        if (text == null) {
            throw new IllegalArgumentException();
        }
        return text.trim();
    }

    // requireNonNull -> no null comparison -> no violation
    public String required(String text) {
        Objects.requireNonNull(text);
        return text;
    }

    // No null check at all -> no violation
    public String echo(String text) {
        return text;
    }

    // Record method — excluded from check
    public record Pair(String first, String second) {

        public String combined(String separator) {
            if (separator == null) {
                return first + second;
            }
            return first + separator + second;
        }
    }

    // Null passed to method with @Nullable param -> no call-site violation
    public void caller() {
        fixKatex(null);
    }

    // Null rejection with != null / else throw -> no violation
    public String strictAlt(String input) {
        if (input != null) {
            return input.trim();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
