package com.caseware.templateupdates.engagement;

/** Mirrors {@code EngagementUpdateStatus.unknownReason} in the API contract. */
public enum UnknownReason {
    NOT_YET_INDEXED,
    TEMPLATE_NOT_IN_CATALOG,
    /** Engagement is on a version the catalog has not seen: the catalog projection is behind. */
    CATALOG_BEHIND
}
