import { Injectable, computed, inject, signal } from '@angular/core';
import { TemplateUpdatesGateway } from '../domain/template-useCase.abstract';
import { EngagementUpdateStatus, PendingUpdateDetail } from '../models/template-interface';
import { Decision, LoadState } from '../models/template-types';
import { StaleDecisionError } from '../utils/template-utils';

/**
 * Single source of truth for the template-updates screen.
 * Components read signals and call methods; they never talk to the gateway directly.
 *
 * Server state (list rows, details) is cached here; UI-only state (confirm dialogs) lives in components.
 */
@Injectable()
export class TemplateUpdatesStore {
  private readonly gateway = inject(TemplateUpdatesGateway);

  /** How often to re-check a summary that is still COMPUTING, and rows with a decision in progress. */
  readonly pollIntervalMs = signal(3000);

  private readonly _rows = signal<EngagementUpdateStatus[]>([]);
  private readonly _listState = signal<LoadState>('idle');
  private readonly _catalogAsOf = signal<string | null>(null);
  private readonly _selectedId = signal<string | null>(null);
  private readonly _detail = signal<PendingUpdateDetail | null>(null);
  private readonly _detailState = signal<LoadState>('idle');
  private readonly _submitting = signal(false);
  private readonly _notice = signal<string | null>(null);
  private pollHandle: ReturnType<typeof setTimeout> | null = null;

  readonly rows = this._rows.asReadonly();
  readonly listState = this._listState.asReadonly();
  readonly catalogAsOf = this._catalogAsOf.asReadonly();
  readonly selectedId = this._selectedId.asReadonly();
  readonly detail = this._detail.asReadonly();
  readonly detailState = this._detailState.asReadonly();
  readonly submitting = this._submitting.asReadonly();
  readonly notice = this._notice.asReadonly();

  readonly pendingCount = computed(() => this._rows().filter((r) => r.status === 'UPDATE_AVAILABLE').length);

  /** Apply/Decline only when the user can actually see what they are deciding on. */
  readonly canDecide = computed(() => {
    const d = this._detail();
    return (
      !!d &&
      d.status === 'UPDATE_AVAILABLE' &&
      d.summary?.availability === 'READY' &&
      d.activeDecision === null &&
      !this._submitting()
    );
  });

  async loadList(): Promise<void> {
    this._listState.set('loading');
    try {
      const response = await this.gateway.listStatuses();
      this._rows.set(response.items);
      this._catalogAsOf.set(response.catalogAsOf);
      this._listState.set('loaded');
    } catch {
      this._listState.set('error');
    }
  }

  async select(engagementId: string): Promise<void> {
    if (this._selectedId() === engagementId) return;
    this._selectedId.set(engagementId);
    this._detail.set(null);
    this._notice.set(null);
    await this.loadDetail(engagementId);
  }

  async decide(decision: Decision): Promise<void> {
    const detail = this._detail();
    if (!detail || !this.canDecide() || detail.currentVersion === null || detail.latestVersion === null) return;

    this._submitting.set(true);
    this._notice.set(null);
    try {
      const accepted = await this.gateway.submitDecision(detail.engagementId, {
        decision,
        fromVersion: detail.currentVersion,
        toVersion: detail.latestVersion,
      });
      // Reflect the accepted-but-not-finished decision immediately, in both the detail and its list row.
      this.patch(detail.engagementId, { activeDecision: accepted });
      this.schedulePoll();
    } catch (error) {
      if (error instanceof StaleDecisionError) {
        this._notice.set('This template was updated again while you were reviewing. The summary below has been refreshed; please review it before deciding.');
        await Promise.all([this.loadList(), this.loadDetail(detail.engagementId)]);
      } else {
        this._notice.set('Your decision could not be submitted. Nothing was changed; please try again.');
      }
    } finally {
      this._submitting.set(false);
    }
  }

  dispose(): void {
    if (this.pollHandle) clearTimeout(this.pollHandle);
    this.pollHandle = null;
  }

  private async loadDetail(engagementId: string): Promise<void> {
    this._detailState.set('loading');
    try {
      const detail = await this.gateway.getPendingUpdate(engagementId);
      if (this._selectedId() !== engagementId) return;
      this._detail.set(detail);
      this._detailState.set('loaded');
      this.patchRow(detail);
      if (detail.summary?.availability === 'COMPUTING' || detail.activeDecision) this.schedulePoll();
    } catch {
      if (this._selectedId() === engagementId) this._detailState.set('error');
    }
  }

  private schedulePoll(): void {
    if (this.pollHandle) return;
    this.pollHandle = setTimeout(async () => {
      this.pollHandle = null;
      const id = this._selectedId();
      await this.loadList();
      if (id) await this.loadDetail(id);
    }, this.pollIntervalMs());
  }

  private patch(engagementId: string, changes: Partial<PendingUpdateDetail>): void {
    this._rows.update((rows) => rows.map((r) => (r.engagementId === engagementId ? { ...r, ...changes } : r)));
    this._detail.update((d) => (d && d.engagementId === engagementId ? { ...d, ...changes } : d));
  }

  private patchRow(detail: PendingUpdateDetail): void {
    const { summary: _summary, ...status } = detail;
    this._rows.update((rows) => rows.map((r) => (r.engagementId === detail.engagementId ? status : r)));
  }
}
