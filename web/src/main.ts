/*
 * Copyright [2026] [envite consulting GmbH]
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

import {
  enableProdMode,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideZonelessChangeDetection
} from '@angular/core';
import { environment } from 'environments/environment';
import { StartupService } from 'app/shared/services/startup/startup.service';
import { httpClientInterceptor, tokenInterceptor } from 'app/shared/interceptors/http-client.interceptor';
import { provideHttpClient, withInterceptors, withXhr, withXsrfConfiguration } from '@angular/common/http';
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
import { provideHotToastConfig } from '@ngneat/hot-toast';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { EngineConfigurationState } from './app/shared/store/engine-configuration-store/engine-configuration.state';
import { ClassificationState } from './app/shared/store/classification-store/classification.state';
import { WorkbasketState } from './app/shared/store/workbasket-store/workbasket.state';
import { AccessItemsManagementState } from './app/shared/store/access-items-management-store/access-items-management.state';
import { FilterState } from './app/shared/store/filter-store/filter.state';
import { WorkplaceState } from './app/shared/store/workplace-store/workplace.state';
import { SettingsState } from './app/shared/store/settings-store/settings.state';
import { TaskWorkflowState } from './app/shared/store/task-store/task.state';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

registerLocaleData(localeDe);

if (environment.production) {
  enableProdMode();
}

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
        SettingsState,
        TaskWorkflowState
      ],
      { developmentMode: !environment.production }
    ),
    withNgxsReduxDevtoolsPlugin({
      disabled: environment.production,
      maxAge: 25
    }),
    withNgxsRouterPlugin(),
    provideZonelessChangeDetection(),
    provideAngularSvgIcon(),
    provideAppInitializer(() => {
      const startupService = inject(StartupService);
      return startupService.load();
    }),
    provideRouter(appRoutes, withHashLocation()),
    provideHttpClient(
      withXhr(),
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
