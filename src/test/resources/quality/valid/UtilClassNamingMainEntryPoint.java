package quality.valid;

public class UtilClassNamingMainEntryPoint {

    // JVM entry-point class with all-static public methods. Without the main-method
    // skip, UtilClassNamingCheck would flag this as «util-shaped without Util/Utils
    // suffix». With the skip, no violation is raised.
    public static class JavaBatchSubmitter {

        private JavaBatchSubmitter() {
        }

        public static void main(String[] args) {
            new JavaBatchSubmitter().run(args);
        }

        public static String describe() {
            return "submitter";
        }

        private void run(String[] args) {
            // pretend work
        }
    }

    // Mixed public/package-private static methods + main → still skipped because
    // the class has a public static main entry point.
    public static class JavaSeedLocalRunner {

        public static void main(String[] args) {
            doStep("a");
        }

        public static int defaults() {
            return 30;
        }

        static void doStep(String label) {
            // pretend work
        }
    }
}
