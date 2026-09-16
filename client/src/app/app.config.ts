import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { TemplateUpdatesGateway } from './template-updates/domain/template-useCase.abstract';
import { FakeTemplateUpdatesGateway } from './template-updates/infrastructure/repository/template-repository.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Swap for an HttpClient-based gateway when a backend exists. Nothing else changes.
    { provide: TemplateUpdatesGateway, useFactory: () => new FakeTemplateUpdatesGateway() },
  ],
};
