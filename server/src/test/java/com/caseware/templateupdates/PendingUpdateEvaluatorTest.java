package com.caseware.templateupdates;

import com.caseware.templateupdates.catalog.TemplateVersion;
import com.caseware.templateupdates.engagement.EngagementTemplateBaseline;
import com.caseware.templateupdates.engagement.PendingUpdateEvaluator;
import com.caseware.templateupdates.engagement.PendingUpdateState;
import com.caseware.templateupdates.engagement.UnknownReason;
import com.caseware.templateupdates.engagement.UpdateStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.caseware.templateupdates.SampleData.CATALOG_AS_OF;
import static com.caseware.templateupdates.SampleData.INDEXED_AT;
import static org.junit.jupiter.api.Assertions.*;

class PendingUpdateEvaluatorTest {

    private final PendingUpdateEvaluator evaluator = new PendingUpdateEvaluator(SampleData.catalog());

    @Test
    void accumulatedVersionsAreAllPendingAndTargetIsTheLatest() {
        // ENG-1007 Bluewater Hospitality: REVIEW-CA v6, while v7 and v8 have been
        // published.
        var eng = EngagementTemplateBaseline.indexed("ENG-1007", "Bluewater Hospitality 2026", "REVIEW-CA", 6,
                INDEXED_AT);

        PendingUpdateState state = evaluator.evaluate(eng, CATALOG_AS_OF);

        assertEquals(UpdateStatus.UPDATE_AVAILABLE, state.status());
        assertEquals(List.of(7, 8), state.pendingVersions().stream().map(TemplateVersion::version).toList());
        assertTrue(state.hasAccumulatedUpdates());
        assertEquals(8, state.targetVersion());
        assertEquals(CATALOG_AS_OF, state.statusAsOf(), "freshness is the older of the two inputs");
    }

    @Test
    void engagementOnLatestVersionIsUpToDate() {
        // ENG-1005 Cedar Peak Services: REVIEW-CA v8 (latest).
        var eng = EngagementTemplateBaseline.indexed("ENG-1005", "Cedar Peak Services 2026", "REVIEW-CA", 8,
                INDEXED_AT);

        PendingUpdateState state = evaluator.evaluate(eng, CATALOG_AS_OF);

        assertEquals(UpdateStatus.UP_TO_DATE, state.status());
        assertTrue(state.pendingVersions().isEmpty());
        assertThrows(IllegalStateException.class, state::targetVersion);
    }

    @Test
    void neverGuessesWhenInformationIsMissingOrInconsistent() {
        var notIndexed = EngagementTemplateBaseline.notYetIndexed("ENG-2001", "Backfill pending", "AUDIT-CA");
        var aheadOfCatalog = EngagementTemplateBaseline.indexed("ENG-2002", "Created from v6", "AUDIT-CA", 6,
                INDEXED_AT);
        var unknownTemplate = EngagementTemplateBaseline.indexed("ENG-2003", "Other product", "RISK-XX", 1, INDEXED_AT);

        assertEquals(UnknownReason.NOT_YET_INDEXED, evaluator.evaluate(notIndexed, CATALOG_AS_OF).unknownReason());
        assertEquals(UnknownReason.CATALOG_BEHIND, evaluator.evaluate(aheadOfCatalog, CATALOG_AS_OF).unknownReason());
        assertEquals(UnknownReason.TEMPLATE_NOT_IN_CATALOG,
                evaluator.evaluate(unknownTemplate, CATALOG_AS_OF).unknownReason());
    }
    
    @Test
    void declinedUpdateIsNotPendingUntilANewerVersionIsPublished() {
        var eng = EngagementTemplateBaseline
                .indexed("ENG-1006", "Westmount Consulting 2026", "REVIEW-CA", 7, INDEXED_AT)
                .withDeclinedThrough(8);

        PendingUpdateState state = evaluator.evaluate(eng, CATALOG_AS_OF);

        assertEquals(UpdateStatus.UP_TO_DATE, state.status());
        assertTrue(state.pendingVersions().isEmpty());
    }
}
