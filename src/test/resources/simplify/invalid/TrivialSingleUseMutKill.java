package com.example;

// Tests visitToken L97 mutation in TrivialSingleUsePrivateMethodCheck
// L97: `log(... ident != null ? ident.getText() : "?")` — replaced return value
@SuppressWarnings("unused")
public class TrivialSingleUseMutKill {
    // Single-use private method with trivial body — should be flagged
    void caller() {
        String result = helper("input");
        System.out.println(result);
    }

    private String helper(String s) {
        return s.trim();
    }
}
