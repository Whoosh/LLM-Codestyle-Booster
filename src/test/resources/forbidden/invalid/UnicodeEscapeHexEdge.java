package com.example;

// Tests isHexDigit with various hex digit ranges
@SuppressWarnings("unused")
public class UnicodeEscapeHexEdge {
    // uppercase hex digits A-F
    String upperHex = "\u0041";
    // lowercase hex digits a-f
    String lowerHex = "\u0061";
    // numeric hex digits 0-9
    String digitHex = "\u0030";
    // mixed case hex
    String mixedHex = "\u00Ab";
}
