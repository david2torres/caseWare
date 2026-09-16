import { Component, computed, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ChangeGroup, ChangeItem, ChangeSummary } from '../../models/template-interface';
import { ChangeKind } from '../../models/template-types';

/**
 * Presentational: renders an already human-readable summary.
 * No diff interpretation happens here — that is the server's job (see DESIGN.md).
 */
@Component({
  selector: 'app-change-summary',
  imports: [DatePipe],
  templateUrl: "./change-summary.html",
})
export class ChangeSummaryView {
  readonly summary = input.required<ChangeSummary>();
  /** Only meaningful when more than one version accumulated. */
  readonly showVersions = input(false);

  readonly groups = computed(() => this.groupByArea(this.summary().items));
  readonly requiredCount = computed(() => this.summary().items.filter((i) => i.requiresResponse).length);

  kindLabel(kind: ChangeKind): string {
    return { ADDED: 'New', MODIFIED: 'Changed', REMOVED: 'Removed' }[kind];
  }

  versionsText(item: ChangeItem): string {
    return item.changedInVersions.map((v) => `v${v}`).join(', ');
  }

  private groupByArea(items: ChangeItem[]): ChangeGroup[] {
    const groups = new Map<string, ChangeItem[]>();
    for (const item of items) {
      groups.set(item.area, [...(groups.get(item.area) ?? []), item]);
    }
    return [...groups].map(([area, grouped]) => ({ area, items: grouped }));
  }
}
