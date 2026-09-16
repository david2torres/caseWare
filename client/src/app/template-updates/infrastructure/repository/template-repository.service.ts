import { TemplateUpdatesGateway } from "../../domain/template-useCase.abstract";
import { CATALOG_AS_OF, ENGAGEMENTS } from "../../models/template-constants";
import { DecisionRequest, EngagementUpdateListResponse, EngagementUpdateStatus, PendingUpdateDetail } from "../../models/template-interface";
import { DecisionAccepted, Decision } from "../../models/template-types";

import { detailFor, StaleDecisionError } from "../../utils/template-utils";

/** In-memory fake backed by fixtures. Simulates latency, a summary still computing, and async decisions. */
export class FakeTemplateUpdatesGateway extends TemplateUpdatesGateway {
    private readonly rows = new Map<string, EngagementUpdateStatus>(ENGAGEMENTS.map((e) => [e.engagementId, structuredClone(e)]));
    private summaryRequests = new Map<string, number>();

    constructor(private readonly latencyMs = 300, private readonly decisionDurationMs = 4000) {
        super();
    }

    async listStatuses(): Promise<EngagementUpdateListResponse> {
        await this.delay();
        return { items: [...this.rows.values()].map((r) => structuredClone(r)), catalogAsOf: CATALOG_AS_OF, generatedAt: new Date().toISOString() };
    }

    async getPendingUpdate(engagementId: string): Promise<PendingUpdateDetail> {
        await this.delay();
        const row = this.require(engagementId);
        const calls = (this.summaryRequests.get(engagementId) ?? 0) + 1;
        this.summaryRequests.set(engagementId, calls);

        if (engagementId === 'ENG-1011') return detailFor(row, 'UNAVAILABLE');
        if (engagementId === 'ENG-1010' && calls === 1) return detailFor(row, 'COMPUTING'); // ready on the next poll
        return detailFor(structuredClone(row), 'READY');
    }

    async submitDecision(engagementId: string, request: DecisionRequest): Promise<DecisionAccepted> {
        await this.delay();
        const row = this.require(engagementId);
        if (row.currentVersion !== request.fromVersion || row.latestVersion !== request.toVersion) {
            throw new StaleDecisionError({ error: 'STALE_UPDATE', currentVersion: row.currentVersion ?? 0, latestVersion: row.latestVersion ?? 0 });
        }
        if (row.activeDecision) {
            return row.activeDecision; // idempotent: same decision already in flight
        }
        const accepted: DecisionAccepted = {
            decisionId: `DEC-${engagementId}-${request.toVersion}`,
            decision: request.decision,
            toVersion: request.toVersion,
            state: 'IN_PROGRESS',
            submittedAt: new Date().toISOString(),
            failureReason: null,
        };
        row.activeDecision = accepted;
        setTimeout(() => this.complete(engagementId, request.decision, request.toVersion), this.decisionDurationMs);
        return structuredClone(accepted);
    }

    private complete(engagementId: string, decision: Decision, toVersion: number): void {
        const row = this.require(engagementId);
        row.activeDecision = null;
        row.pendingVersions = [];
        row.status = 'UP_TO_DATE';
        // Apply moves the baseline. Decline keeps the content version but records the decision server-side,
        // so the engagement is no longer "pending" until a version newer than toVersion is published.
        if (decision === 'APPLY') row.currentVersion = toVersion;
        else row.declinedThroughVersion = toVersion;
        row.statusAsOf = new Date().toISOString();
    }

    private require(engagementId: string): EngagementUpdateStatus {
        const row = this.rows.get(engagementId);
        if (!row) throw new Error(`Unknown engagement ${engagementId}`);
        return row;
    }

    private delay(): Promise<void> {
        return new Promise((resolve) => setTimeout(resolve, this.latencyMs));
    }
}
