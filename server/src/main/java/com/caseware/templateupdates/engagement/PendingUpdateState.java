package com.caseware.templateupdates.engagement;

import com.caseware.templateupdates.catalog.TemplateVersion;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * The pending template-update state of one engagement.
 * Server-side equivalent of the {@code EngagementUpdateStatus} DTO in the API contract.
 *
 * @param pendingVersions every published version newer than the baseline, oldest first
 *                        (more than one element means updates have accumulated)
 * @param statusAsOf      freshness: the older of "baseline indexed at" and "catalog updated at"
 */
public record PendingUpdateState(
        String engagementId,
        String templateId,
        UpdateStatus status,
        UnknownReason unknownReason,
        Integer currentVersion,
        Integer latestVersion,
        List<TemplateVersion> pendingVersions,
        Instant statusAsOf) {

    public PendingUpdateState {
        Objects.requireNonNull(engagementId);
        Objects.requireNonNull(templateId);
        Objects.requireNonNull(status);
        Objects.requireNonNull(statusAsOf);
        pendingVersions = List.copyOf(pendingVersions);
        if ((status == UpdateStatus.UNKNOWN) != (unknownReason != null)) {
            throw new IllegalArgumentException("unknownReason is required iff status is UNKNOWN");
        }
        if ((status == UpdateStatus.UPDATE_AVAILABLE) == pendingVersions.isEmpty()) {
            throw new IllegalArgumentException("UPDATE_AVAILABLE iff there are pending versions");
        }
    }

    /** Version reached by applying. Always the latest: intermediate versions are not offered. */
    public int targetVersion() {
        if (status != UpdateStatus.UPDATE_AVAILABLE) throw new IllegalStateException("no pending update");
        return pendingVersions.getLast().version();
    }

    public boolean hasAccumulatedUpdates() {
        return pendingVersions.size() > 1;
    }
}
