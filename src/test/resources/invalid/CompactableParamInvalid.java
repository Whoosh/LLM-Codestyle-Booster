package test;

import java.time.LocalDateTime;

public class CompactableParamInvalid {

    // Case 1: record with loosely packed parameters — each continuation line fits on prev
    public record QuizRow(long idx, String question,
                          String answer, String theme,
                          float difficulty,
                          LocalDateTime createdAt) {
    }

    // Case 2: method with loosely packed params
    public void process(String input,
                        int count) {
    }

    // Case 3: constructor with loose params
    public CompactableParamInvalid(String name,
                                   int value) {
    }
}
