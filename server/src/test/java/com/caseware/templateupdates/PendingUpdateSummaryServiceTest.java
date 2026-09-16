package com.caseware.templateupdates;

import com.caseware.templateupdates.engagement.EngagementTemplateBaseline;
import com.caseware.templateupdates.engagement.PendingUpdateEvaluator;
import com.caseware.templateupdates.engagement.PendingUpdateState;
import com.caseware.templateupdates.summary.ChangeDescriber;
import com.caseware.templateupdates.summary.ChangeItem;
import com.caseware.templateupdates.summary.ChangeKind;
import com.caseware.templateupdates.summary.ChangeSummary;
import com.caseware.templateupdates.summary.ItemType;
import com.caseware.templateupdates.summary.PendingUpdateSummaryService;
import com.caseware.templateupdates.summary.SummaryAvailability;
import com.caseware.templateupdates.summary.TemplateVocabulary;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PendingUpdateSummaryServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-15T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private static final TemplateVocabulary SECTION_NAMES = (templateId, version, key) ->
            Optional.ofNullable(Map.of("planning", "Planning", "materiality", "Materiality",
                    "analytics", "Analytical Procedures").get(key));

    private final PendingUpdateState bluewaterOnV6 = new PendingUpdateEvaluator(SampleData.catalog()).evaluate(
            EngagementTemplateBaseline.indexed("ENG-1007", "Bluewater Hospitality 2026", "REVIEW-CA", 6, SampleData.INDEXED_AT),
            SampleData.CATALOG_AS_OF);

    @Test
    void accumulatedUpdateShowsNetChangeAndWhichVersionsTouchedIt() {
        var diffs = new SampleData.InMemoryDiffSource()
                .with(SampleData.reviewV6toV8())
                .with(SampleData.reviewV6toV7())
                .with(SampleData.reviewV7toV8());
        var service = new PendingUpdateSummaryService(diffs, new ChangeDescriber(SECTION_NAMES), CLOCK);

        ChangeSummary summary = service.summarize(bluewaterOnV6);

        assertEquals(SummaryAvailability.READY, summary.availability());
        assertEquals(6, summary.fromVersion());
        assertEquals(8, summary.toVersion());
        assertEquals(5, summary.items().size());

        // Tolerance went 0.15 -> 0.12 (v7) -> 0.10 (v8): user sees the NET effect, and that two versions touched it.
        ChangeItem tolerance = byPath(summary, "/sections/analytics/procedures/2/tolerance");
        assertEquals("Procedure tolerance decreased", tolerance.title());
        assertEquals("Analytical Procedures", tolerance.area());
        assertEquals("0.15", tolerance.before());
        assertEquals("0.1", tolerance.after());
        assertEquals(List.of(7, 8), tolerance.changedInVersions());

        ChangeItem goingConcern = byPath(summary, "/sections/completion/checklists/going-concern");
        assertEquals(ChangeKind.ADDED, goingConcern.kind());
        assertEquals(ItemType.CHECKLIST, goingConcern.itemType());
        assertEquals("Completion", goingConcern.area(), "falls back to humanised key when no display name is known");
        assertEquals(List.of(8), goingConcern.changedInVersions());

        assertEquals(List.of(7), byPath(summary, "/sections/inquiries/questions/12").changedInVersions());
    }

    @Test
    void summaryNotYetComputedOrFailedIsExplicitNotEmpty() {
        var describer = new ChangeDescriber(SECTION_NAMES);

        var computing = new PendingUpdateSummaryService(new SampleData.InMemoryDiffSource(), describer, CLOCK)
                .summarize(bluewaterOnV6);
        assertEquals(SummaryAvailability.COMPUTING, computing.availability());

        var failed = new PendingUpdateSummaryService(
                new SampleData.InMemoryDiffSource().failing("REVIEW-CA", 6, 8, "DIFF_SERVICE_TIMEOUT"), describer, CLOCK)
                .summarize(bluewaterOnV6);
        assertEquals(SummaryAvailability.UNAVAILABLE, failed.availability());
        assertEquals("DIFF_SERVICE_TIMEOUT", failed.unavailableReason());
    }

    @Test
    void rawChangesBecomePractitionerFriendlyText() {
        var describer = new ChangeDescriber(SECTION_NAMES);
        var diff = SampleData.auditV3toV4();

        ChangeItem newQuestion = describer.describe("AUDIT-CA", 4, diff.changes().get(0));
        assertEquals("New question added", newQuestion.title());
        assertEquals("Planning", newQuestion.area());
        assertTrue(newQuestion.requiresResponse());
        assertTrue(newQuestion.detail().contains("Were any new fraud risk factors identified during planning?"));

        ChangeItem threshold = describer.describe("AUDIT-CA", 4, diff.changes().get(1));
        assertEquals("Threshold percent decreased", threshold.title());
        assertEquals("5", threshold.before());
        assertEquals("4.5", threshold.after());

        ChangeItem removedProcedure = describer.describe("AUDIT-CA", 4, diff.changes().get(2));
        assertEquals("Procedure removed", removedProcedure.title());
        assertFalse(removedProcedure.requiresResponse());
        assertTrue(removedProcedure.detail().contains("Confirm legacy risk classification"));
    }

    private static ChangeItem byPath(ChangeSummary summary, String path) {
        return summary.items().stream().filter(i -> i.technicalPath().equals(path)).findFirst().orElseThrow();
    }
}
