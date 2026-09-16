import { Decision, UpdateStatus, UnknownReason, SummaryAvailability, ChangeKind, ItemType } from "./template-types";

export interface PublishedVersion {
    version: number;
    publishedAt: string;
}

/** A decision the server accepted but the engagement system has not finished (loading a file takes ~1 min). */
export interface ActiveDecision {
    decisionId: string;
    decision: Decision;
    toVersion: number;
    state: 'IN_PROGRESS' | 'FAILED';
    submittedAt: string;
    failureReason: string | null;
}

/** GET /api/engagements/template-updates */
export interface EngagementUpdateListResponse {
    items: EngagementUpdateStatus[];
    /** Last TemplatePublished event reflected in this response. */
    catalogAsOf: string;
    generatedAt: string;
}


export interface EngagementUpdateStatus {
    engagementId: string;
    engagementName: string;
    templateId: string;
    templateDisplayName: string;
    status: UpdateStatus;
    unknownReason: UnknownReason | null;
    /** null = not known yet (engagement not indexed). Never defaulted to a guess. */
    currentVersion: number | null;
    latestVersion: number | null;
    /** Oldest first. length > 1 means updates have accumulated. */
    pendingVersions: PublishedVersion[];
    activeDecision: ActiveDecision | null;
    /** Set after a Decline; the engagement is pending again only when a version > this is published. */
    declinedThroughVersion: number | null;
    /** Freshness of THIS row: older of "engagement indexed at" and "catalogAsOf". */
    statusAsOf: string;
}

/** GET /api/engagements/{engagementId}/template-updates/pending */
export interface PendingUpdateDetail extends EngagementUpdateStatus {
    /** null when status !== 'UPDATE_AVAILABLE'. */
    summary: ChangeSummary | null;
}

/** Net changes baseline -> latest, already human-readable (transformed on the server). */
export interface ChangeSummary {
    availability: SummaryAvailability;
    fromVersion: number;
    toVersion: number;
    /** Empty unless availability === 'READY'. */
    items: ChangeItem[];
    computedAt: string | null;
    unavailableReason: string | null;
}

export interface ChangeItem {
    changeId: string;
    kind: ChangeKind;
    itemType: ItemType;
    area: string;
    title: string;
    detail: string | null;
    before: string | null;
    after: string | null;
    /** Pending versions that touched this item. Empty = attribution unknown. */
    changedInVersions: number[];
    requiresResponse: boolean;
    /** Raw JSON pointer, for support/traceability only. */
    technicalPath: string;
}

/** POST /api/engagements/{engagementId}/template-updates/decisions */
export interface DecisionRequest {
    decision: Decision;
    /** Optimistic concurrency: what the user reviewed. */
    fromVersion: number;
    toVersion: number;
}

/** 409 Conflict body: a newer version was published (or the baseline changed) while the user was reviewing. */
export interface StaleDecisionProblem {
    error: 'STALE_UPDATE';
    currentVersion: number;
    latestVersion: number;
}

export interface ChangeGroup {
    area: string;
    items: ChangeItem[];
}
