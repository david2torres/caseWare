package com.caseware.templateupdates.engagement;

import com.caseware.templateupdates.catalog.TemplateCatalog;
import com.caseware.templateupdates.catalog.TemplateCatalogEntry;
import com.caseware.templateupdates.catalog.TemplateVersion;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Pure logic: (engagement baseline, template catalog) -> pending update state.
 * No I/O and no engagement-file loading, so it is cheap enough to run for every engagement on every list request.
 *
 * Assumption for this excerpt (per the brief): no apply/decline history; the recorded version is the baseline.
 * With history, "is pending" would compare latest against max(appliedVersion, declinedThroughVersion).
 */
public final class PendingUpdateEvaluator {

    private final TemplateCatalog catalog;

    public PendingUpdateEvaluator(TemplateCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
    }

    public PendingUpdateState evaluate(EngagementTemplateBaseline engagement, Instant catalogAsOf) {
        Objects.requireNonNull(engagement);
        Objects.requireNonNull(catalogAsOf);

        OptionalInt baseline = engagement.baseline();
        if (baseline.isEmpty()) {
            return unknown(engagement, UnknownReason.NOT_YET_INDEXED, null, null, catalogAsOf);
        }
        int current = baseline.getAsInt();
        Instant asOf = older(engagement.indexedAt(), catalogAsOf);

        TemplateCatalogEntry entry = catalog.find(engagement.templateId()).orElse(null);
        if (entry == null) {
            return unknown(engagement, UnknownReason.TEMPLATE_NOT_IN_CATALOG, current, null, asOf);
        }

        int latest = entry.latestVersion();
        if (current > latest) {
            // Engagement was created from a version our catalog projection hasn't received yet.
            // Answering "up to date" would be a guess; surface it instead.
            return unknown(engagement, UnknownReason.CATALOG_BEHIND, current, latest, asOf);
        }

        List<TemplateVersion> pending = entry.versionsAfter(current);
        UpdateStatus status = pending.isEmpty() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UPDATE_AVAILABLE;
        return new PendingUpdateState(engagement.engagementId(), engagement.templateId(), status, null,
                current, latest, pending, asOf);
    }

    private static PendingUpdateState unknown(EngagementTemplateBaseline e, UnknownReason reason,
                                              Integer current, Integer latest, Instant asOf) {
        return new PendingUpdateState(e.engagementId(), e.templateId(), UpdateStatus.UNKNOWN, reason,
                current, latest, List.of(), asOf);
    }

    private static Instant older(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }
}
