package com.caseware.templateupdates;

import com.caseware.templateupdates.engagement.EngagementTemplateBaseline;
import com.caseware.templateupdates.engagement.PendingUpdateEvaluator;
import com.caseware.templateupdates.observability.FirmUpdateSweep;
import com.caseware.templateupdates.observability.UpdateImpact;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.caseware.templateupdates.SampleData.CATALOG_AS_OF;
import static com.caseware.templateupdates.SampleData.INDEXED_AT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Why this matters: after a publish we must be able to say how many engagements a firm has to review,
 * and how much of the firm we cannot measure yet, without loading a single engagement file.
 */
class FirmUpdateSweepTest {

    private final FirmUpdateSweep sweep =
            new FirmUpdateSweep("FIRM-001", new PendingUpdateEvaluator(SampleData.catalog()));

    @Test
    void countsAffectedAndUnmeasurableEngagementsForThePublishedTemplateOnly() {
        List<EngagementTemplateBaseline> index = List.of(
                EngagementTemplateBaseline.indexed("ENG-1005", "Cedar Peak", "REVIEW-CA", 8, INDEXED_AT),      // up to date
                EngagementTemplateBaseline.indexed("ENG-1006", "Westmount", "REVIEW-CA", 7, INDEXED_AT),       // affected
                EngagementTemplateBaseline.indexed("ENG-1007", "Bluewater", "REVIEW-CA", 6, INDEXED_AT),       // affected
                EngagementTemplateBaseline.notYetIndexed("ENG-1008", "Summit", "REVIEW-CA"),                   // unmeasurable
                EngagementTemplateBaseline.indexed("ENG-1002", "Maple Ridge", "AUDIT-CA", 4, INDEXED_AT));     // other template

        UpdateImpact impact = sweep.sweep(index, "REVIEW-CA", 8, CATALOG_AS_OF);

        assertEquals("FIRM-001", impact.firmId());
        assertEquals(4, impact.engagementsOnTemplate(), "AUDIT-CA engagements are not counted");
        assertEquals(2, impact.engagementsAffected());
        assertEquals(1, impact.engagementsUnknown());
        assertEquals(0.25, impact.unknownRatio());
        assertEquals(CATALOG_AS_OF, impact.checkedAt(), "checkedAt is the per-firm heartbeat");
    }

    @Test
    void reportsZeroImpactWhenNoEngagementUsesTheTemplate() {
        UpdateImpact impact = sweep.sweep(
                List.of(EngagementTemplateBaseline.indexed("ENG-1002", "Maple Ridge", "AUDIT-CA", 4, INDEXED_AT)),
                "REVIEW-CA", 8, CATALOG_AS_OF);

        assertEquals(0, impact.engagementsOnTemplate());
        assertEquals(0, impact.engagementsAffected());
        assertTrue(impact.unknownRatio() == 0.0);
    }
}
