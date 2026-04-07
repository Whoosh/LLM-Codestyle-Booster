package test;

import static java.lang.Math.PI;

public class StaticImportCandidateValid {

    public void method() {
        // PI already static-imported — not flagged even with multiple uses
        double area = PI * 5 * 5;
        double circ = 2 * PI * 5;
    }
}
