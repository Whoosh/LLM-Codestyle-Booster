package test;

import java.util.regex.Pattern;

public class StaticImportCandidateInvalid {

    public void method() {
        // References Math.PI twice without static import — fires
        double area = Math.PI * 5 * 5;
        double circ = 2 * Math.PI * 5;
        // References Integer.MAX_VALUE twice — fires
        int cap = Integer.MAX_VALUE;
        int limit = Integer.MAX_VALUE / 2;
        // Single reference — still fires (every qualified constant ref)
        long max = Long.MIN_VALUE;
        // Qualified constant as method-call receiver — must fire (regression from parent-DOT guard)
        java.util.regex.Matcher m = Holder.PATTERN.matcher("x");
    }

    static class Holder {
        public static final Pattern PATTERN = Pattern.compile("a");
    }
}
