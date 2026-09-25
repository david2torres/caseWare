package com.caseware.templateupdates.observability;

import com.caseware.templateupdates.engagement.EngagementTemplateBaseline;
import com.caseware.templateupdates.engagement.PendingUpdateEvaluator;
import com.caseware.templateupdates.engagement.PendingUpdateState;
import com.caseware.templateupdates.engagement.UpdateStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Runs inside the firm's own boundary when a TemplateVersionPublished event arrives.
 *
 * It reads only the firm's index (never an engagement file) and emits one metric record per firm.
 * The user-facing read path stays compute-on-read; this sweep exists for observability, not for the UI,
 * so a slow or failed sweep can never block or corrupt what a user sees.
 */
public final class FirmUpdateSweep {

    private final String firmId;
    private final PendingUpdateEvaluator evaluator;

    public FirmUpdateSweep(String firmId, PendingUpdateEvaluator evaluator) {
        this.firmId = Objects.requireNonNull(firmId);
        this.evaluator = Objects.requireNonNull(evaluator);
    }

    /**
     * @param index         the firm's engagement index rows (already scoped to this firm by the data boundary)
     * @param templateId    the template whose new version was published
     * @param version       the published version
     * @param catalogAsOf   freshness of the catalog used for the evaluation
     */
    public UpdateImpact sweep(List<EngagementTemplateBaseline> index, String templateId, int version, Instant catalogAsOf) {
        int onTemplate = 0;
        int affected = 0;
        int unknown = 0;

        for (EngagementTemplateBaseline engagement : index) {
            if (!engagement.templateId().equals(templateId)) continue;
            onTemplate++;

            PendingUpdateState state = evaluator.evaluate(engagement, catalogAsOf);
            if (state.status() == UpdateStatus.UNKNOWN) {
                unknown++;
            } else if (state.status() == UpdateStatus.UPDATE_AVAILABLE) {
                affected++;
            }
        }
        return new UpdateImpact(firmId, templateId, version, onTemplate, affected, unknown, catalogAsOf);
    }
}
