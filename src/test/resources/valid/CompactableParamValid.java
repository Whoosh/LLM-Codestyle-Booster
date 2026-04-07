package test;

import java.time.LocalDateTime;

public class CompactableParamValid {

    // Case 1: all on one line — nothing to compact
    public void singleLine(String a, int b, float c) {
    }

    // Case 2: genuinely long params — next line does NOT fit on prev (combined > 180)
    public void longParams(String firstVeryLongParameterNameThatTakesUpLotsOfSpace, String secondVeryLongParameterNameAlsoTakingUpSpace,
                           String thirdExtremelyLongParameterNameThatDefinitelyDoesNotFitOnThePreviousLineWhenCombinedWithItBecauseItIsTooLongAtOneHundredEighty) {
    }

    // Case 3: method with one param — always single line
    public void oneParam(String only) {
    }
}
