import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TemplateUpdatesStore } from '../../store/template-updates.store';
import { EngagementUpdateList } from '../engagement-update/engagement-update-list';
import { PendingUpdatePanel } from '../pending-update/pending-update-panel';

@Component({
  selector: 'app-template-updates-page',
  imports: [DatePipe, EngagementUpdateList, PendingUpdatePanel],
  providers: [TemplateUpdatesStore],
  templateUrl: "./template-update-page.html",
})

export class TemplateUpdatesPage implements OnInit {
  protected readonly store = inject(TemplateUpdatesStore);

  constructor() {
    inject(DestroyRef).onDestroy(() => this.store.dispose());
  }

  ngOnInit(): void {
    void this.store.loadList();
  }
}
