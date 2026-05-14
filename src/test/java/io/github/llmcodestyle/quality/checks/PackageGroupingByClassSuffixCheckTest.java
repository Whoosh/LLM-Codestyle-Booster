package io.github.llmcodestyle.quality.checks;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.llmcodestyle.utils.TestCheckSupportUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class PackageGroupingByClassSuffixCheckTest {

    @Test
    void groupOfFivePlusOtherFlagsTheGroupMember() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PackageGroupingByClassSuffixCheck.class, "quality/invalid/grouping_with_other/AlphaCheck.java", Map.of());

        assertEquals(1, violations.size(), "Expected single violation, got: " + format(violations));
        String message = violations.get(0).getMessage();
        assertTrue(message.contains("AlphaCheck"), "Message must contain class name: " + message);
        assertTrue(message.contains("Check"), "Message must contain suffix: " + message);
        assertTrue(message.contains("checks"), "Message must contain pluralized subpackage: " + message);
    }

    @Test
    void otherClassInMixedDirectoryIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PackageGroupingByClassSuffixCheck.class, "quality/invalid/grouping_with_other/HelperUtility.java", Map.of());

        assertTrue(violations.isEmpty(), "The 'other' class itself must not be flagged: " + format(violations));
    }

    @Test
    void twoLargeGroupsInSameDirectoryBothFireOnTheirOwnMembers() throws Exception {
        List<AuditEvent> checkSide = runTreeWalkerCheck(PackageGroupingByClassSuffixCheck.class, "quality/invalid/grouping_two_big_groups/AlphaCheck.java", Map.of());
        assertEquals(1, checkSide.size(), "Check member should fire: " + format(checkSide));
        assertTrue(checkSide.get(0).getMessage().contains("checks"), "Should hint 'checks' subpackage");

        List<AuditEvent> detectorSide = runTreeWalkerCheck(PackageGroupingByClassSuffixCheck.class, "quality/invalid/grouping_two_big_groups/PrimaryDetector.java", Map.of());
        assertEquals(1, detectorSide.size(), "Detector member should fire: " + format(detectorSide));
        assertTrue(detectorSide.get(0).getMessage().contains("detectors"), "Should hint 'detectors' subpackage");
    }

    @Test
    void homogeneousPackageWithNoOthersIsNotFlagged() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PackageGroupingByClassSuffixCheck.class, "quality/valid/grouping_only_one_suffix/AlphaCheck.java", Map.of());

        assertTrue(violations.isEmpty(), "5 *Check with no others must not fire: " + format(violations));
    }

    @Test
    void groupBelowThresholdIsNotFlaggedEvenWithOthers() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PackageGroupingByClassSuffixCheck.class, "quality/valid/grouping_below_threshold/AlphaCheck.java", Map.of());

        assertTrue(violations.isEmpty(), "Group of 4 < threshold must not fire: " + format(violations));
    }

    @Test
    void pojoOnlyAsidesAreNotCountedAsOthers() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(PackageGroupingByClassSuffixCheck.class, "quality/valid/grouping_only_pojos_aside/AlphaCheck.java", Map.of());

        assertTrue(violations.isEmpty(), "POJO siblings must not count as 'other' classes: " + format(violations));
    }

    @Test
    void pojoFileItselfIsNeverFlagged() throws Exception {
        assertNoViolationsOn("quality/valid/grouping_only_pojos_aside/MyRecord.java", "Records are POJOs and never flagged");
        assertNoViolationsOn("quality/valid/grouping_only_pojos_aside/MyEnum.java", "Enums are POJOs and never flagged");
        assertNoViolationsOn("quality/valid/grouping_only_pojos_aside/MyInterface.java", "Interfaces are POJOs and never flagged");
        assertNoViolationsOn("quality/valid/grouping_only_pojos_aside/CustomerDto.java", "*Dto-suffixed classes are POJOs and never flagged");
    }

    @Test
    void minGroupSizePropertyLowersTheThreshold() throws Exception {
        List<AuditEvent> violations = runTreeWalkerCheck(
            PackageGroupingByClassSuffixCheck.class,
            "quality/valid/grouping_below_threshold/AlphaCheck.java",
            Map.of("minGroupSize", "3"));

        assertEquals(1, violations.size(), "With minGroupSize=3, the 4-element Check group should fire: " + format(violations));
    }

    @Test
    void setMinGroupSizeIsDirectlyInvocable() {
        PackageGroupingByClassSuffixCheck check = new PackageGroupingByClassSuffixCheck();
        check.setMinGroupSize(7);
        assertDoesNotThrow(() -> check.setMinGroupSize(3));
    }

    @Test
    void lastCamelWordHandlesAcronymsAndSimpleNames() {
        assertEquals("Check", PackageGroupingByClassSuffixCheck.lastCamelWord("UtilClassNamingCheck"));
        assertEquals("Parser", PackageGroupingByClassSuffixCheck.lastCamelWord("XMLParser"));
        assertEquals("Foo", PackageGroupingByClassSuffixCheck.lastCamelWord("Foo"));
        assertNull(PackageGroupingByClassSuffixCheck.lastCamelWord(""));
    }

    @Test
    void pluralizeCoversCommonEndings() {
        assertEquals("checks", PackageGroupingByClassSuffixCheck.pluralize("Check"));
        assertEquals("detectors", PackageGroupingByClassSuffixCheck.pluralize("Detector"));
        assertEquals("repositories", PackageGroupingByClassSuffixCheck.pluralize("Repository"));
        assertEquals("strategies", PackageGroupingByClassSuffixCheck.pluralize("Strategy"));
        assertEquals("buses", PackageGroupingByClassSuffixCheck.pluralize("Bus"));
        assertEquals("boxes", PackageGroupingByClassSuffixCheck.pluralize("Box"));
        assertEquals("buzzes", PackageGroupingByClassSuffixCheck.pluralize("Buzz"));
        assertEquals("patches", PackageGroupingByClassSuffixCheck.pluralize("Patch"));
        assertEquals("brushes", PackageGroupingByClassSuffixCheck.pluralize("Brush"));
        assertEquals("days", PackageGroupingByClassSuffixCheck.pluralize("Day"));
    }

    private static void assertNoViolationsOn(String fixture, String message) throws Exception {
        assertTrue(runTreeWalkerCheck(PackageGroupingByClassSuffixCheck.class, fixture, Map.of()).isEmpty(), message);
    }
}
