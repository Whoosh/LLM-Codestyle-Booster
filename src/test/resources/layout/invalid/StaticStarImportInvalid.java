package com.example;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticStarImportInvalid {

    void test() {
        emptyList();
        emptyMap();
        assertEquals(1, 1);
    }
}
