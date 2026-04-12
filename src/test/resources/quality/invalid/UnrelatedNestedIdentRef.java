package com.example;

// Tests isIdentReference L163 in UnrelatedNestedTypeCheckBase
// The mutation on L163 negates: `!DECLARATION_PARENT_TYPES.contains(parent.getType()) || !node.equals(parent.findFirstToken(IDENT))`
@SuppressWarnings("unused")
public class UnrelatedNestedIdentRef {

    private String outerField = "outer";

    void outerMethod() { }

    // Nested record that references outerField — should NOT be flagged
    record RelatedRecord(String name) {
        String use() {
            return outerField;
        }
    }

    // Nested record that does NOT reference any outer member — SHOULD be flagged
    record UnrelatedRecord(String value) {
        String self() {
            return value;
        }
    }
}
