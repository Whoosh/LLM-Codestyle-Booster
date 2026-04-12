package com.example;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

// Tests extractEnclosingClassName L220 NO_COVERAGE in DuplicateMethodBodyCheck
// Need a method with enough body nodes to not be trivial, inside a nested class
@SuppressWarnings("unused")
public class DuplicateMethodBodyNestedClass {

    static class InnerA {
        String loadResource(String name) {
            InputStream is = getClass().getResourceAsStream(name);
            if (is == null) {
                throw new IllegalArgumentException("not found: " + name);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (Exception e) {
                throw new RuntimeException("failed to read: " + name, e);
            }
        }
    }

    static class InnerB {
        String loadResource(String path) {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new IllegalArgumentException("not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (Exception e) {
                throw new RuntimeException("failed to read: " + path, e);
            }
        }
    }
}
