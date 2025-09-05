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

import { of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from 'app/../environments/environment';
import { inject, Injectable, Injector } from '@angular/core';
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { map } from 'rxjs/operators';
import { WindowRefService } from 'app/shared/services/window/window.service';

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

  load(): Promise<any> {
    return this.loadEnvironment();
  }

  // TODO: refactor this
  getEnvironmentFilePromise() {
    return this.httpClient
      .get<any>('environments/data-sources/environment-information.json')
      .pipe(
        map((jsonFile) => {
          if (jsonFile && jsonFile.kadaiRestUrl) {
            environment.kadaiRestUrl = jsonFile.kadaiRestUrl;
          }

          if (jsonFile && jsonFile.kadaiLogoutUrl) {
            environment.kadaiLogoutUrl = jsonFile.kadaiLogoutUrl;
          }
        })
      )
      .toPromise()
      .catch(() => of(true));
  }

  getKadaiRestUrl() {
    return environment.kadaiRestUrl;
  }

  getKadaiLogoutUrl() {
    return environment.kadaiLogoutUrl;
  }

  private loadEnvironment() {
    return this.getEnvironmentFilePromise()
      .then(() => this.kadaiEngineService.getUserInformation())
      .catch((error) => {
        // this.window.nativeWindow.location.href = environment.kadaiRestUrl + '/login';
      });
  }
}
