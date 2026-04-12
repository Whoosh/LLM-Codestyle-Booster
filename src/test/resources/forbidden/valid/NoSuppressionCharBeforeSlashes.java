package com.example;

// If inChar toggling is broken, the following line might be misinterpreted
public class NoSuppressionCharBeforeSlashes {
    // Char containing forward slash — should NOT affect // detection on the next line
    char slash = '/';
    int x = 1; // this is a normal comment, not a suppression
}
