package com.caseware.templateupdates.engagement;

/** Mirrors {@code EngagementUpdateStatus.status} in the API contract. */
public enum UpdateStatus {
    UP_TO_DATE,
    UPDATE_AVAILABLE,
    /** We cannot answer truthfully yet (not indexed, unknown template, catalog lagging). Never guess. */
    UNKNOWN
}
