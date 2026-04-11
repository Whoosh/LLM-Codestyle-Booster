package quality.invalid;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class DuplicateMethodBodyJavaPromptBuilder {

    private final String systemPromptText = readResource("batch/java-system-prompt.md");

    public String systemPrompt() {
        return systemPromptText;
    }

    // Same loadResource body but renamed to readResource with different local name —
    // the normalizer should still see an identical structure.
    private static String readResource(String location) {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(location)) {
            if (stream == null) {
                throw new IllegalStateException("Resource not found: " + location);
            }
            return new String(stream.readAllBytes(), UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load resource: " + location, ex);
        }
    }
}
