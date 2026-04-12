package quality.invalid;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class DuplicateMethodBodyPhysicsBatchInserter {

    private static final Object MAPPER = new Object();

    private DuplicateMethodBodyPhysicsBatchInserter() {
    }

    static List<Object> parseJsonLines(Path path) throws IOException {
        List<Object> entries = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path, UTF_8)) {
            for (String row = br.readLine(); row != null; row = br.readLine()) {
                if (!row.isBlank()) {
                    entries.add(MAPPER.toString() + row);
                }
            }
        }
        return entries;
    }
}
