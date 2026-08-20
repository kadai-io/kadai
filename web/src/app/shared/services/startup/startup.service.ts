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

import { firstValueFrom, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from 'app/../environments/environment';
import { inject, Injectable, Injector } from '@angular/core';
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { catchError, tap } from 'rxjs/operators';
import { WindowRefService } from 'app/shared/services/window/window.service';

interface EnvironmentConfig {
  kadaiRestUrl?: string;
  kadaiLogoutUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class StartupService {
  private httpClient = inject(HttpClient);
  private kadaiEngineService = inject(KadaiEngineService);
  private injector = inject(Injector);
  private window = inject(WindowRefService);

  public get router(): Router {
    return this.injector.get(Router);
  }

  load(): Promise<EnvironmentConfig> {
    return this.loadEnvironment();
  }

  // TODO: refactor this - Done ?
  getEnvironmentFilePromise() {
    return firstValueFrom(
      this.httpClient.get<EnvironmentConfig>('environments/data-sources/environment-information.json').pipe(
        tap((config) => {
          if (config?.kadaiRestUrl) {
            environment.kadaiRestUrl = config.kadaiRestUrl;
          }

          if (config?.kadaiLogoutUrl) {
            environment.kadaiLogoutUrl = config.kadaiLogoutUrl;
          }
        }),
        catchError((error) => {
          console.warn('Failed to load environment configuration:', error);
          return of(null);
        })
      )
    ).then(() => void 0);
  }

  getKadaiRestUrl() {
    return environment.kadaiRestUrl;
  }

  getKadaiLogoutUrl() {
    return environment.kadaiLogoutUrl;
  }

  private loadEnvironment(): Promise<EnvironmentConfig> {
    return this.getEnvironmentFilePromise()
      .then(() => this.kadaiEngineService.getUserInformation())
      .catch((error) => {
        // this.window.nativeWindow.location.href = environment.kadaiRestUrl + '/login';
      });
  }
}
