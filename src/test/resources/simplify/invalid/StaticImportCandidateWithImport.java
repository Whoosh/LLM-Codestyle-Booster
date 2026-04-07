package test;

import static java.lang.Math.PI;

public class StaticImportCandidateWithImport {

    public void method() {
        // PI is already static-imported — qualified usage must NOT fire
        double area = Math.PI * 5 * 5;
        double circ = 2 * Math.PI * 5;
        // MAX_VALUE is NOT static-imported — qualified usage MUST fire
        int cap = Integer.MAX_VALUE;
    }
}
