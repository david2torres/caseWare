import { Component, input, output, signal } from '@angular/core';
import { Decision } from '../../models/template-types';

/** Presentational: two-step confirm. The pending confirmation is local UI state, not store state. */
@Component({
  selector: 'app-decision-actions',
  templateUrl: "./decision-action.html"
})
export class DecisionActions {
  readonly toVersion = input.required<number>();
  readonly disabled = input(false);
  readonly decided = output<Decision>();

  readonly confirming = signal<Decision | null>(null);

  public confirm(decision: Decision): void {
    this.confirming.set(null);
    this.decided.emit(decision);
  }
}
