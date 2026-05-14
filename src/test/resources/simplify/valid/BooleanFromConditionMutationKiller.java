package simplify.valid;

public class BooleanFromConditionMutationKiller {

    private int value;

    // Case 1: Non-VARIABLE_DEF followed by if that looks like a flip
    // If booleanLiteralVarName returns non-null for non-VARIABLE_DEF, this would false-positive
    public boolean nonVarDefFollowedByFlipIf() {
        value = 0;
        if (value > 0) {
            value = 1;
        }
        return value > 0;
    }

    // Case 2: VARIABLE_DEF with non-boolean type followed by boolean-looking flip
    // If the type check's return null is mutated, this int var could be treated as boolean
    public int intVarWithBooleanLikingFlip() {
        int flag = 0;
        if (value > 0) {
            flag = 1;
        }
        return flag;
    }

    // Case 3: boolean var without literal initializer (init from method call)
    // Exercises literalKindOfInit returning 0 for non-literal
    public boolean boolFromMethod() {
        boolean result = isPositive();
        if (value > 0) {
            result = false;
        }
        return result;
    }

    // Case 4: boolean var with no initializer (just declaration)
    // Exercises ASSIGN == null check
    public boolean noInit() {
        boolean flag;
        if (value > 100) {
            flag = false;
        } else {
            flag = value > 0;
        }
        return flag;
    }

    private boolean isPositive() {
        if (value == Integer.MIN_VALUE) {
            throw new IllegalStateException();
        }
        return value > 0;
    }
}
