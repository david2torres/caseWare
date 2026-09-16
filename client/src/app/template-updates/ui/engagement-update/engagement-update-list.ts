import { Component, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { EngagementUpdateStatus } from '../../models/template-interface';

/** Presentational: renders rows, emits selection. No store, no gateway. */
@Component({
  selector: 'app-engagement-update-list',
  imports: [DatePipe],
  templateUrl: "./engagement-update-list.html",
})
export class EngagementUpdateList {
  readonly rows = input.required<EngagementUpdateStatus[]>();
  readonly selectedId = input<string | null>(null);
  readonly pendingCount = input(0);
  readonly selected = output<string>();

  public statusText(row: EngagementUpdateStatus): string {
    if (row.activeDecision?.state === 'IN_PROGRESS') {
      return `${row.activeDecision.decision === 'APPLY' ? 'Applying' : 'Declining'} update to v${row.activeDecision.toVersion}…`;
    }

    if (row.activeDecision?.state === 'FAILED') return 'Last decision failed — open to retry';
    switch (row.status) {
      case 'UPDATE_AVAILABLE': {
        const n = row.pendingVersions.length;
        return n > 1 ? `${n} updates pending (up to v${row.latestVersion})` : `Update available (v${row.latestVersion})`;
      }
      case 'UP_TO_DATE':
        return row.declinedThroughVersion ? `Up to date (declined v${row.declinedThroughVersion})` : 'Up to date';
      case 'UNKNOWN':
        return row.unknownReason === 'NOT_YET_INDEXED' ? 'Checking… (not scanned yet)' : 'Status unavailable';
    }
  }
}
