package com.example;

// Tests extractMethodName L158 and isFlowBlock L199 in PureSingleUseLocalVariableCheck
@SuppressWarnings("unused")
public class PureSingleUseMutKill {
    // Pure single-use var with DOT method call, used two statements later (not the next)
    void dotPureCall(String input) {
        int val = input.length();
        int other = 42;
        System.out.println(val + other);
    }

    // Pure single-use var inside an if block (exercises isFlowBlock L199 for LITERAL_IF)
    void insideIfBlock(boolean cond, String text) {
        if (cond) {
            String inner = text.substring(1);
            int x = 1;
            System.out.println(inner + x);
        }
    }

    // Pure single-use var inside a for loop (exercises isFlowBlock -> isLoopOrCondition)
    void insideForLoop(String text) {
        for (int i = 0; i < 3; i++) {
            String item = text.trim();
            int y = 2;
            System.out.println(item + y);
        }
    }

    // Pure single-use var inside try block (exercises isFlowBlock -> isExceptionBlock)
    void insideTryBlock(String text) {
        try {
            String inner = text.strip();
            int z = 3;
            System.out.println(inner + z);
        } catch (Exception e) {
            // ignore
        }
    }
}
