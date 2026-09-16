import { Component, computed, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TemplateUpdatesStore } from '../../store/template-updates.store';
import { ChangeSummaryView } from '../change-summary/change-summary';
import { DecisionActions } from '../decision-action/decision-actions';

/** Container: reads the selected engagement from the store and wires presentational children. */
@Component({
  selector: 'app-pending-update-panel',
  imports: [DatePipe, ChangeSummaryView, DecisionActions],
  templateUrl: "./pending-update-panel.html"
})
export class PendingUpdatePanel {
  protected readonly store = inject(TemplateUpdatesStore);
  protected readonly accumulated = computed(() => (this.store.detail()?.pendingVersions.length ?? 0) > 1);
}
