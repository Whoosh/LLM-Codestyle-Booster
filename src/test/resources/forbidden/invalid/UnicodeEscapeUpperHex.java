package test;

public class UnicodeEscapeUpperHex {

    // Uses uppercase hex digits: A, B, C, D, E, F
    // Exercises isHexDigit's uppercase branch (c >= 'A' && c <= 'F')
    private static final String HEX_A = "alpha \u0041"; // A
    private static final String HEX_B = "bravo \u0042"; // B
    private static final String HEX_FF = "max \u00FF"; // FF uppercase
    private static final String HEX_DE = "delta \u00DE"; // DE uppercase
}
