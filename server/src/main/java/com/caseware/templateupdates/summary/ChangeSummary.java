package com.caseware.templateupdates.summary;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Net, human-readable summary of what applying the update would change: baseline -> latest.
 * Mirrors {@code ChangeSummary} in the API contract.
 */
public record ChangeSummary(
        SummaryAvailability availability,
        int fromVersion,
        int toVersion,
        List<ChangeItem> items,
        Instant computedAt,
        String unavailableReason) {

    public ChangeSummary {
        Objects.requireNonNull(availability);
        items = List.copyOf(items);
        if (availability != SummaryAvailability.READY && !items.isEmpty()) {
            throw new IllegalArgumentException("only READY summaries carry items");
        }
    }

    public static ChangeSummary ready(int from, int to, List<ChangeItem> items, Instant computedAt) {
        return new ChangeSummary(SummaryAvailability.READY, from, to, items, computedAt, null);
    }

    public static ChangeSummary computing(int from, int to) {
        return new ChangeSummary(SummaryAvailability.COMPUTING, from, to, List.of(), null, null);
    }

    public static ChangeSummary unavailable(int from, int to, String reason) {
        return new ChangeSummary(SummaryAvailability.UNAVAILABLE, from, to, List.of(), null, reason);
    }
}
