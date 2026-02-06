import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/http/interceptors/auth.interceptor';
import { correlationInterceptor } from './core/http/interceptors/correlation.interceptor';
import { errorInterceptor } from './core/http/interceptors/error.interceptor';
import { orgInterceptor } from './core/http/interceptors/org.interceptor';
import { initializeKeycloak } from './keycloak/keycloak-init';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        correlationInterceptor,
        authInterceptor,
        orgInterceptor,
        errorInterceptor,
      ]),
    ),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
    },
  ],
};
