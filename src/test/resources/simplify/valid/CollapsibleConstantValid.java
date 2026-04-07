package test;

import java.util.regex.Pattern;

public class CollapsibleConstantValid {

    // Case 1: single literal — no concatenation, nothing to collapse
    static final String SIMPLE = "hello";

    // Case 2: single numeric literal
    static final int NUMBER = 42;

    // Case 3: method call in expression — not collapsible
    static final String FROM_METHOD = String.valueOf(42);

    // Case 4: constant + method call — not collapsible
    static final String PREFIX = "prefix";
    static final String DYNAMIC = PREFIX + String.valueOf(System.currentTimeMillis());

    // Case 5: non-static-final — not checked
    final String instanceConcat = "a" + "b";

    // Case 6: non-final — not checked
    static String mutableConcat = "a" + "b";

    // Case 7: reference to constant from another class (can't resolve)
    static final String EXTERNAL = Integer.MAX_VALUE + "_suffix";

    // Case 8: expression with non-literal operator (multiplication, not +)
    static final int PRODUCT = 6 * 7;

    // Case 9: constant referencing non-literal constant (chain involves method call)
    static final String COMPUTED = String.format("%d", 42);
    static final String USES_COMPUTED = COMPUTED + "!";

    // Case 10: single constant reference — no concatenation
    static final String ALIAS = PREFIX;

    // Case 11: boolean constant — not a concat-able type
    static final boolean FLAG = true;

    // Case 12: Pattern — method call initializer
    static final Pattern DIGITS = Pattern.compile("\\d+");

    // Case 13: constant + DOT-qualified reference (OtherClass.CONST)
    static final String WITH_DOT = PREFIX + CollapsibleConstantValid.SIMPLE;

    // Case 14: empty class with no fields — no crash

    // Case 15: array with only single literals — no concatenation
    private static final String[] CLEAN_ARRAY = {"a", "b", "c"};

    // Case 16: array with method call in element — not collapsible
    private static final String[] DYNAMIC_ARRAY = {
        "literal",
        String.valueOf(42),
    };

    // Case 17: array element referencing external constant — can't resolve
    private static final String[] EXTERNAL_ARRAY = {
        Integer.MAX_VALUE + "_x",
    };

    // Case 18: non-static-final array — not checked
    final String[] INSTANCE_ARRAY = {"a" + "b"};

    // Case 19: method body with only 1 constant between dynamic calls — no run
    static String methodWithSingleConstant() {
        return dynamicValue() + PREFIX + dynamicValue();
    }

    // Case 20: method body with all dynamic parts — no run
    static String methodAllDynamic() {
        return dynamicValue() + "x" + dynamicValue();
    }

    // Case 21: method body with constants separated by dynamic calls — no run of 3
    static String methodInterleaved() {
        return PREFIX + dynamicValue() + SIMPLE + dynamicValue() + PREFIX;
    }

    private static String dynamicValue() {
        return "dynamic";
    }
}
