package test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Every variable here must NOT be flagged by SingleUseLocalVariable or PureSingleUseLocalVariable.
 * Inlining any of them would worsen asymptotic complexity or move computation into a
 * more-frequently-executed context.
 *
 * Organized by: [loop type] x [initializer type] x [use position].
 */
public class AsymptoticSafetyTraps {

    // =============================================
    // A. Classic for-loop
    // =============================================

    // A1: cached size in for-condition → O(1) becomes O(n) calls to size()
    void forConditionSize(List<String> items) {
        int size = items.size();
        for (int i = 0; i < size; i++) {
            process(items.get(i));
        }
    }

    // A2: cached value in for-update → evaluated every iteration
    void forUpdateCached(List<String> items, int[] counters) {
        int step = counters[0];
        for (int i = 0; i < items.size(); i += step) {
            process(items.get(i));
        }
    }

    // A3: cached value used in for-body directly
    void forBodyDirect(List<String> items) {
        String prefix = computeExpensive(items);
        for (int i = 0; i < items.size(); i++) {
            System.out.println(prefix + items.get(i));
        }
    }

    // A4: cached value used inside if-condition inside for-body
    void forBodyNestedIf(int[] arr, List<String> items) {
        int threshold = arr[0];
        for (String item : items) {
            if (item.length() > threshold) {
                process(item);
            }
        }
    }

    // A5: cached value used in method call arg inside for-body
    void forBodyMethodArg(List<String> items) {
        String separator = getSeparator();
        for (String item : items) {
            emit(item, separator);
        }
    }

    // A6: cached value used in ternary inside for-body
    void forBodyTernary(int[] config, List<String> items) {
        int limit = config[0];
        for (int i = 0; i < items.size(); i++) {
            process(i < limit ? items.get(i) : "default");
        }
    }

    // =============================================
    // B. For-each loop
    // =============================================

    // B1: O(n) computation cached before for-each body
    void forEachBodyExpensive(List<String> items) {
        String joined = String.join(",", items);
        for (String item : items) {
            System.out.println(joined + item);
        }
    }

    // B2: pure getter cached before for-each
    void forEachBodyPureGetter(Map<String, String> config, List<String> items) {
        String mode = config.get("mode");
        for (String item : items) {
            System.out.println(mode + ": " + item);
        }
    }

    // B3: .length() cached before for-each
    void forEachBodyLength(String pattern, List<String> items) {
        int patLen = pattern.length();
        for (String item : items) {
            if (item.length() >= patLen) {
                process(item);
            }
        }
    }

    // B4: parseInt cached before for-each
    void forEachBodyParseInt(String threshold, List<String> items) {
        int limit = Integer.parseInt(threshold);
        for (String item : items) {
            if (item.length() > limit) {
                process(item);
            }
        }
    }

    // B5: arithmetic cached before for-each
    void forEachBodyArithmetic(int a, int b, List<String> items) {
        int combined = a * b + 1;
        for (String item : items) {
            System.out.println(combined + item);
        }
    }

    // =============================================
    // C. While / do-while
    // =============================================

    // C1: cached value in while-condition
    void whileConditionCached(List<String> items, int[] config) {
        int maxSize = config[0];
        while (items.size() < maxSize) {
            items.add("fill");
        }
    }

    // C2: cached value in while-body
    void whileBodyCached(Iterator<String> it) {
        String prefix = computePrefix();
        while (it.hasNext()) {
            System.out.println(prefix + it.next());
        }
    }

    // C3: cached value in do-while body
    void doWhileBodyCached(List<String> items, int[] arr) {
        int step = arr[0];
        int idx = 0;
        do {
            process(items.get(idx));
            idx += step;
        } while (idx < items.size());
    }

    // C4: cached value in do-while condition
    void doWhileConditionCached(List<String> items, int[] config) {
        int limit = config[0];
        int count = 0;
        do {
            count++;
        } while (count < limit);
    }

    // C5: pure .get() before while-body
    void whileBodyPureGet(Map<String, Integer> config, List<String> items) {
        int batchSize = config.get("batch");
        int i = 0;
        while (i < items.size()) {
            processBatch(items.subList(i, Math.min(i + batchSize, items.size())));
            i += batchSize;
        }
    }

    // =============================================
    // D. Lambda / stream (repeating via functional)
    // =============================================

    // D1: cached value inside forEach lambda
    void forEachLambda(List<String> items) {
        String prefix = computePrefix();
        items.forEach(item -> System.out.println(prefix + item));
    }

    // D2: cached value inside stream().map() lambda
    void streamMapLambda(List<String> items) {
        String suffix = getSeparator();
        items.stream().map(item -> item + suffix).forEach(System.out::println);
    }

    // D3: cached value inside stream().filter() lambda
    void streamFilterLambda(List<String> items, int[] config) {
        int minLen = config[0];
        items.stream().filter(item -> item.length() >= minLen).forEach(this::process);
    }

    // D4: cached value inside stream().reduce() accumulator (lambda executes per element)
    void streamReduceLambda(List<Integer> numbers, int[] config) {
        int base = config[0];
        numbers.stream().reduce(0, (acc, n) -> acc + n + base);
    }

    // D5: cached value inside removeIf lambda
    void removeIfLambda(List<String> items, int[] config) {
        int maxLen = config[0];
        items.removeIf(item -> item.length() > maxLen);
    }

    // D6: cached value inside sort Comparator lambda
    void sortComparatorLambda(List<String> items) {
        String reference = computePrefix();
        items.sort(Comparator.comparingInt(item -> Math.abs(item.length() - reference.length())));
    }

    // D7: cached value inside replaceAll lambda
    void replaceAllLambda(List<String> items) {
        String prefix = computePrefix();
        items.replaceAll(item -> prefix + item);
    }

    // D8: cached value used inside stream filter lambda
    void streamWithCachedPredicate(List<String> items, String pattern) {
        int patLen = pattern.length();
        items.stream().filter(s -> s.length() == patLen).forEach(this::process);
    }

    // =============================================
    // E. Nested loops (quadratic risk)
    // =============================================

    // E1: cached in outer for, used in inner for → O(n) becomes O(n*m)
    void nestedForFor(List<List<String>> matrix) {
        for (List<String> row : matrix) {
            String header = computeHeader(row);
            for (String cell : row) {
                System.out.println(header + cell);
            }
        }
    }

    // E2: cached in outer for, used in inner while
    void nestedForWhile(List<String> items) {
        for (String item : items) {
            int len = item.length();
            int i = 0;
            while (i < len) {
                System.out.println(item.charAt(i));
                i++;
            }
        }
    }

    // E3: cached in outer for, used in inner forEach lambda
    void nestedForLambda(List<List<String>> matrix) {
        for (List<String> row : matrix) {
            String rowId = row.toString();
            row.forEach(cell -> System.out.println(rowId + ": " + cell));
        }
    }

    // E4: cached in outer for, used in inner stream
    void nestedForStream(Map<String, List<String>> groups) {
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            String key = entry.getKey();
            entry.getValue().stream().map(v -> key + "=" + v).forEach(System.out::println);
        }
    }

    // E5: cached in outer while, used in inner for-each
    void nestedWhileForEach(List<List<String>> batches) {
        int idx = 0;
        while (idx < batches.size()) {
            String batchId = String.valueOf(idx);
            for (String item : batches.get(idx)) {
                System.out.println(batchId + ": " + item);
            }
            idx++;
        }
    }

    // =============================================
    // F. Multiple cached variables before same loop
    // =============================================

    // F1: two variables cached, both used in loop body
    void twoCachedVarsInLoop(int[] arr, List<String> items) {
        int lo = arr[0];
        int hi = arr[1];
        for (String item : items) {
            if (item.length() >= lo && item.length() <= hi) {
                process(item);
            }
        }
    }

    // F2: three variables cached, used at different nesting levels
    void threeCachedVars(Map<String, String> config, List<List<String>> matrix) {
        String prefix = config.get("prefix");
        String suffix = config.get("suffix");
        for (List<String> row : matrix) {
            String rowTag = computeHeader(row);
            for (String cell : row) {
                System.out.println(prefix + rowTag + cell + suffix);
            }
        }
    }

    // =============================================
    // G. Deferred use + loop (PureSingleUseLocalVariable specific)
    // =============================================

    // G1: pure var defined, then unrelated statement, then loop with var
    void deferredThenLoop(int[] arr, List<String> items) {
        int limit = arr[0];
        System.out.println("processing...");
        for (String item : items) {
            if (item.length() > limit) {
                process(item);
            }
        }
    }

    // G2: pure var defined, two statements between, then lambda
    void deferredThenLambda(int[] arr, List<String> items) {
        int threshold = arr[0];
        System.out.println("start");
        System.out.println("items: " + items.size());
        items.removeIf(item -> item.length() < threshold);
    }

    // G3: pure var defined, then try block with loop inside
    void deferredThenTryWithLoop(int[] config, List<String> items) {
        int batchSize = config[0];
        try {
            for (int i = 0; i < items.size(); i += batchSize) {
                processBatch(items.subList(i, Math.min(i + batchSize, items.size())));
            }
        } catch (Exception ignored) {
        }
    }

    // G4: pure var defined, used after two unrelated statements then in loop
    void deferredLongGap(int[] arr, List<String> items) {
        int maxLen = arr[0];
        System.out.println("Results:");
        System.out.println("Count: " + items.size());
        for (String item : items) {
            if (item.length() <= maxLen) {
                process(item);
            }
        }
    }

    // =============================================
    // H. Constructor / new object before loop
    // =============================================

    // H1: StringBuilder before loop (1 alloc vs n allocs)
    void stringBuilderBeforeLoop(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append(",");
        }
        System.out.println(sb);
    }

    // H2: Predicate created once, used in stream
    void predicateBeforeStream(List<String> items) {
        Predicate<String> filter = s -> !s.isEmpty();
        items.stream().filter(filter).forEach(this::process);
    }

    // H3: Set created once for O(1) lookups in loop
    void setBeforeLoop(List<String> allowed, List<String> items) {
        Set<String> allowedSet = Set.copyOf(allowed);
        for (String item : items) {
            if (allowedSet.contains(item)) {
                process(item);
            }
        }
    }

    // =============================================
    // Helpers
    // =============================================

    private void process(String s) { }

    private void emit(String item, String sep) { }

    private void processBatch(List<String> batch) { }

    private String computePrefix() { return "p"; }

    private String computeExpensive(List<String> items) { return String.join(",", items); }

    private String computeHeader(List<String> row) { return "h"; }

    private String getSeparator() { return ","; }
}
