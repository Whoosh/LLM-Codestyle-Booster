package com.example;

// Tests isHexDigit returning false for non-hex chars
@SuppressWarnings("unused")
public class UnicodeEscapeNonHex {
    // \uGGGG - not valid hex digits, so isHexDigit returns false for 'G'
    // This is a raw string that happens to contain backslash-u followed by non-hex
    String notUnicode = "prefix\\uZZZZ suffix";
    // Backslash-u followed by only 3 chars at end of line
    String truncated = "end\\u00";
}
