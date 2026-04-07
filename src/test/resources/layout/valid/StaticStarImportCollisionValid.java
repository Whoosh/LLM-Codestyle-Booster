package com.example;

import static java.util.Collections.*;
import static java.lang.Math.PI;
import static com.example.Constants.PI;

/**
 * Explicit imports are OK when there is a name collision
 * (PI imported from both Math and Constants).
 */
public class StaticStarImportCollisionValid {

    void test() {
        emptyList();
    }
}
