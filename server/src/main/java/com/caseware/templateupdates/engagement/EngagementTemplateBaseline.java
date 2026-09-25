package com.caseware.templateupdates.engagement;

import java.time.Instant;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Row of the per-firm "engagement template index": the template an engagement is on,
 * captured WITHOUT loading the engagement file (populated by create/apply hooks and a one-off backfill).
 *
 * @param baselineVersion null while the engagement has not been indexed yet (backfill still running)
 * @param indexedAt       when the baseline was last written; null if never indexed
 */
public record EngagementTemplateBaseline(
        String engagementId,
        String engagementName,
        String templateId,
        Integer baselineVersion,
        Instant indexedAt) {

    public EngagementTemplateBaseline {
        Objects.requireNonNull(engagementId, "engagementId");
        Objects.requireNonNull(engagementName, "engagementName");
        Objects.requireNonNull(templateId, "templateId");
        if ((baselineVersion == null) != (indexedAt == null)) {
            throw new IllegalArgumentException("baselineVersion and indexedAt must be both set or both null");
        }
    }

    public static EngagementTemplateBaseline indexed(String id, String name, String templateId, int version, Instant indexedAt) {
        return new EngagementTemplateBaseline(id, name, templateId, version, indexedAt);
    }

    public static EngagementTemplateBaseline notYetIndexed(String id, String name, String templateId) {
        return new EngagementTemplateBaseline(id, name, templateId, null, null);
    }

    public OptionalInt baseline() {
        return baselineVersion == null ? OptionalInt.empty() : OptionalInt.of(baselineVersion);
    }
}
