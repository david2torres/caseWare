import { EngagementUpdateListResponse, PendingUpdateDetail, DecisionRequest } from "../models/template-interface";
import { DecisionAccepted } from "../models/template-types";

/**
 * Port the store depends on. A real implementation would wrap HttpClient
 * (e.g. `firstValueFrom(http.get<EngagementUpdateListResponse>(...))`); out of scope here.
 */
export abstract class TemplateUpdatesGateway {
    abstract listStatuses(): Promise<EngagementUpdateListResponse>;
    abstract getPendingUpdate(engagementId: string): Promise<PendingUpdateDetail>;
    abstract submitDecision(engagementId: string, request: DecisionRequest): Promise<DecisionAccepted>;
}