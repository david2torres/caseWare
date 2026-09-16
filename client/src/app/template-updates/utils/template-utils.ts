import { CATALOG_AS_OF, NAMES, SUMMARY_ITEMS, VERSIONS } from "../models/template-constants";
import { EngagementUpdateStatus, PendingUpdateDetail, StaleDecisionProblem } from "../models/template-interface";


export function detailFor(status: EngagementUpdateStatus, availability: 'READY' | 'COMPUTING' | 'UNAVAILABLE'): PendingUpdateDetail {
    if (status.status !== 'UPDATE_AVAILABLE' || status.currentVersion === null || status.latestVersion === null) {
        return { ...status, summary: null };
    }
    return {
        ...status,
        summary: {
            availability,
            fromVersion: status.currentVersion,
            toVersion: status.latestVersion,
            items: availability === 'READY' ? SUMMARY_ITEMS[status.engagementId] ?? [] : [],
            computedAt: availability === 'READY' ? '2026-09-15T09:00:00Z' : null,
            unavailableReason: availability === 'UNAVAILABLE' ? 'DIFF_NOT_AVAILABLE' : null,
        },
    };
}

export function row(engagementId: string, engagementName: string, templateId: string, currentVersion: number): EngagementUpdateStatus {
    const versions = VERSIONS[templateId];
    const latestVersion = versions[versions.length - 1].version;
    const pendingVersions = versions.filter((v) => v.version > currentVersion);
    return {
        engagementId,
        engagementName,
        templateId,
        templateDisplayName: NAMES[templateId],
        status: pendingVersions.length ? 'UPDATE_AVAILABLE' : 'UP_TO_DATE',
        unknownReason: null,
        currentVersion,
        latestVersion,
        pendingVersions,
        activeDecision: null,
        declinedThroughVersion: null,
        statusAsOf: CATALOG_AS_OF,
    };
}

/** Thrown for HTTP 409 on decision submit. */
export class StaleDecisionError extends Error {
    constructor(readonly problem: StaleDecisionProblem) {
        super('STALE_UPDATE');
    }
}