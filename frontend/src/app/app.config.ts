import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { RxStompService } from './services/rx-stomp.service';
import { rxStompServiceFactory } from './services/rx-stomp-service-factory';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(), provideAnimationsAsync(), provideAnimationsAsync(),
    {provide: RxStompService, useFactory: rxStompServiceFactory}
  ]
};
