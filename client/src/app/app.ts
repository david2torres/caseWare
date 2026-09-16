import { Component } from '@angular/core';
import { TemplateUpdatesPage } from './template-updates/ui/template-updates/template-updates-page';

@Component({
  selector: 'app-root',
  imports: [TemplateUpdatesPage],
  template: `<app-template-updates-page />`,
})
export class App {}
