package test;

import java.util.List;

public class UseIsEmptyInvalid {

    public void checkLength(String s) {
        // length() > 0 → !s.isEmpty()
        if (s.length() > 0) {
            System.out.println("not empty 1");
        }
        // length() != 0 → !s.isEmpty()
        if (s.length() != 0) {
            System.out.println("not empty 2");
        }
        // length() == 0 → s.isEmpty()
        if (s.length() == 0) {
            System.out.println("empty 3");
        }
        // length() >= 1 → !s.isEmpty()
        if (s.length() >= 1) {
            System.out.println("not empty 4");
        }
        // length() < 1 → s.isEmpty()
        if (s.length() < 1) {
            System.out.println("empty 5");
        }
    }

    public void checkSize(List<String> list) {
        // size() > 0 → !list.isEmpty()
        if (list.size() > 0) {
            System.out.println("not empty 6");
        }
        // size() != 0 → !list.isEmpty()
        if (list.size() != 0) {
            System.out.println("not empty 7");
        }
        // size() == 0 → list.isEmpty()
        if (list.size() == 0) {
            System.out.println("empty 8");
        }
    }

    public void checkReversed(String s, List<String> list) {
        // 0 < s.length() → !s.isEmpty()
        if (0 < s.length()) {
            System.out.println("not empty 9");
        }
        // 0 == s.length() → s.isEmpty()
        if (0 == s.length()) {
            System.out.println("empty 10");
        }
        // 0 != list.size() → !list.isEmpty()
        if (0 != list.size()) {
            System.out.println("not empty 11");
        }
        // 1 <= list.size() → !list.isEmpty()
        if (1 <= list.size()) {
            System.out.println("not empty 12");
        }
        // 1 > s.length() → s.isEmpty()
        if (1 > s.length()) {
            System.out.println("empty 13");
        }
    }

    public void checkStringBuilder(StringBuilder sb) {
        // sb.length() > 0 — same as NoSystemOutInProductionCheck.buildDottedName
        if (sb.length() > 0) {
            sb.append('.');
        }
    }
}
