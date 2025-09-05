/*
 * Copyright [2025] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

import { enableProdMode, inject, LOCALE_ID, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { environment } from 'environments/environment';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { StartupService } from 'app/shared/services/startup/startup.service';
import { NotificationService } from './app/shared/services/notifications/notification.service';
import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpHeaders,
  HttpInterceptorFn,
  HttpRequest,
  HttpXsrfTokenExtractor,
  provideHttpClient,
  withInterceptors,
  withXsrfConfiguration
} from '@angular/common/http';
import { bootstrapApplication } from '@angular/platform-browser';
import { appRoutes } from './app/app.routes';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { provideStore } from '@ngxs/store';
import { withNgxsReduxDevtoolsPlugin } from '@ngxs/devtools-plugin';
import { withNgxsRouterPlugin } from '@ngxs/router-plugin';
import { AppComponent } from './app/app.component';
import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
import { provideRouter, withHashLocation } from '@angular/router';
import { tap } from 'rxjs/operators';
import { provideHotToastConfig } from '@ngneat/hot-toast';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { EngineConfigurationState } from './app/shared/store/engine-configuration-store/engine-configuration.state';
import { ClassificationState } from './app/shared/store/classification-store/classification.state';
import { WorkbasketState } from './app/shared/store/workbasket-store/workbasket.state';
import { AccessItemsManagementState } from './app/shared/store/access-items-management-store/access-items-management.state';
import { FilterState } from './app/shared/store/filter-store/filter.state';
import { WorkplaceState } from './app/shared/store/workplace-store/workplace.state';
import { SettingsState } from './app/shared/store/settings-store/settings.state';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

registerLocaleData(localeDe);

if (environment.production) {
  enableProdMode();
}

export const tokenInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  let request = req;
  if (!environment.production) {
    request = req.clone({ headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=') });
  }
  return next(request);
};

export const httpClientInterceptor: HttpInterceptorFn = (request: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const requestInProgressService = inject(RequestInProgressService);
  const tokenExtractor = inject(HttpXsrfTokenExtractor);
  const notificationService = inject(NotificationService);

  let req = request.clone();
  if (req.headers.get('Content-Type') === 'multipart/form-data') {
    const headers = new HttpHeaders();
    req = req.clone({ headers });
  } else {
    req = req.clone({ setHeaders: { 'Content-Type': 'application/hal+json' } });
  }
  let token = tokenExtractor.getToken();
  if (token !== null) {
    req = req.clone({ setHeaders: { 'X-XSRF-TOKEN': token } });
  }
  if (!environment.production) {
    req = req.clone({ headers: req.headers.set('Authorization', 'Basic YWRtaW46YWRtaW4=') });
  }
  return next(req).pipe(
    tap(
      () => {},
      (error) => {
        requestInProgressService.setRequestInProgress(false);
        if (
          error.status !== 404 &&
          (!(error instanceof HttpErrorResponse) || error.url.indexOf('environment-information.json') === -1)
        ) {
          const { key, messageVariables } = error.error.error || {
            key: 'FALLBACK',
            messageVariables: {}
          };
          notificationService.showError(key, messageVariables);
        }
      }
    )
  );
};

bootstrapApplication(AppComponent, {
  providers: [
    provideStore(
      [
        EngineConfigurationState,
        ClassificationState,
        WorkbasketState,
        AccessItemsManagementState,
        FilterState,
        WorkplaceState,
        SettingsState
      ],
      { developmentMode: !environment.production }
    ),
    withNgxsReduxDevtoolsPlugin({
      disabled: environment.production,
      maxAge: 25
    }),
    withNgxsRouterPlugin(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideAngularSvgIcon(),
    provideAppInitializer(() => {
      const startupService = inject(StartupService);
      return startupService.load();
    }),
    provideRouter(appRoutes, withHashLocation()),
    provideHttpClient(
      withInterceptors([tokenInterceptor, httpClientInterceptor]),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN'
      })
    ),
    {
      provide: LOCALE_ID,
      useValue: 'de'
    },
    provideHotToastConfig({
      style: {
        'max-width': '520px'
      }
    }),
    provideCharts(withDefaultRegisterables()),
    provideAnimationsAsync()
  ]
}).then((err) => console.error(err));
