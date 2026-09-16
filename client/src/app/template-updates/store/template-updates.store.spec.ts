import { TestBed } from '@angular/core/testing';
import { TemplateUpdatesGateway } from '../domain/template-useCase.abstract';
import { ENGAGEMENTS } from '../models/template-constants';
import { PendingUpdateDetail, DecisionRequest, EngagementUpdateListResponse } from '../models/template-interface';
import { DecisionAccepted } from '../models/template-types';
import { detailFor, StaleDecisionError } from '../utils/template-utils';
import { TemplateUpdatesStore } from './template-updates.store';

/**
 * Why this test matters: between opening the summary and clicking Apply, a newer template version can be
 * published. The user must never apply something they did not review. The server rejects with 409; the
 * client must refresh what the user sees and must not show the decision as in progress.
 */
class StubGateway extends TemplateUpdatesGateway {
  // ENG-1006 starts on v7 with v8 pending...
  private detail: PendingUpdateDetail = detailFor(ENGAGEMENTS.find((e) => e.engagementId === 'ENG-1006')!, 'READY');
  requests: DecisionRequest[] = [];
  detailLoads = 0;

  async listStatuses(): Promise<EngagementUpdateListResponse> {
    return { items: [this.detail], catalogAsOf: '2026-09-15T00:00:00Z', generatedAt: '2026-09-15T00:00:00Z' };
  }

  async getPendingUpdate(): Promise<PendingUpdateDetail> {
    this.detailLoads++;
    return structuredClone(this.detail);
  }

  async submitDecision(_id: string, request: DecisionRequest): Promise<DecisionAccepted> {
    this.requests.push(request);
    // ...but v9 was published while the user was reading the v7 -> v8 summary.
    this.detail = {
      ...this.detail,
      latestVersion: 9,
      pendingVersions: [...this.detail.pendingVersions, { version: 9, publishedAt: '2026-09-15T08:00:00Z' }],
      summary: { ...this.detail.summary!, toVersion: 9, computedAt: '2026-09-15T08:01:00Z' },
    };
    throw new StaleDecisionError({ error: 'STALE_UPDATE', currentVersion: 7, latestVersion: 9 });
  }
}

describe('TemplateUpdatesStore', () => {
  it('refreshes the summary instead of applying when a newer version was published during review', async () => {
    const gateway = new StubGateway();
    TestBed.configureTestingModule({
      providers: [TemplateUpdatesStore, { provide: TemplateUpdatesGateway, useValue: gateway }],
    });
    const store = TestBed.inject(TemplateUpdatesStore);

    await store.select('ENG-1006');
    expect(store.canDecide()).toBe(true);

    await store.decide('APPLY');

    expect(gateway.requests).toEqual([{ decision: 'APPLY', fromVersion: 7, toVersion: 8 }]);
    expect(gateway.detailLoads).toBe(2);
    expect(store.detail()?.latestVersion).toBe(9);
    expect(store.detail()?.activeDecision).toBeNull();
    expect(store.notice()).toContain('updated again');
    expect(store.canDecide()).toBe(true); // user can now review v9 and decide again
    store.dispose();
  });
});
