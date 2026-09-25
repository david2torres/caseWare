package com.caseware.templateupdates.observability;

import java.time.Instant;
import java.util.Objects;

/**
 * Result of one firm's pending-update sweep after a template version is published.
 *
 * Answers the three operational questions the requirements ask for:
 *  - how many engagements a newly published version affects (per firm and in aggregate),
 *  - whether the check actually ran for this firm (checkedAt is the heartbeat),
 *  - how much of the firm is unmeasurable right now (engagementsUnknown -> index gaps).
 */
public record UpdateImpact(
        String firmId,
        String templateId,
        int publishedVersion,
        int engagementsOnTemplate,
        int engagementsAffected,
        int engagementsUnknown,
        Instant checkedAt) {

    public UpdateImpact {
        Objects.requireNonNull(firmId, "firmId");
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(checkedAt, "checkedAt");
        if (engagementsAffected + engagementsUnknown > engagementsOnTemplate) {
            throw new IllegalArgumentException("affected + unknown cannot exceed the engagements on the template");
        }
    }

    /** Share of the firm's engagements we could not classify; alarm when this stays above a threshold. */
    public double unknownRatio() {
        return engagementsOnTemplate == 0 ? 0.0 : (double) engagementsUnknown / engagementsOnTemplate;
    }
}
