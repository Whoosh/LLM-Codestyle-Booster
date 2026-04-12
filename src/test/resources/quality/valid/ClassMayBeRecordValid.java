package quality.valid;

import java.io.IOException;

public class ClassMayBeRecordValid {

    // Has instance method beyond standard → not a pure data carrier
    static final class Service {

        private String name;

        String process(String input) {
            return name + ": " + input;
        }
    }

    // Not final → can't be a record
    static class Mutable {

        private int value;
    }

    // Extends a class → can't be a record
    static final class Extended extends IOException {

        private int code;
    }

    // No instance fields → utility class, not a data carrier
    static final class Utils {

        static String format(String s) {
            return s.trim();
        }
    }

    // Non-private instance field → skip
    static final class Exposed {

        int x;
        int y;
    }
}
