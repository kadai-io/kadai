/*
 * Copyright [2024] [envite consulting GmbH]
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

import { inject, NgModule } from '@angular/core';
import { Router, RouterModule, Routes, UrlTree } from '@angular/router';
import { RoutingUploadComponent } from './components/routing-upload/routing-upload.component';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { KadaiEngineService } from '../../shared/services/kadai-engine/kadai-engine.service';

const routes: Routes = [
  {
    path: '',
    canActivate: [
      (): Observable<boolean | UrlTree> => {
        const kadaiEngineService = inject(KadaiEngineService);
        const router = inject(Router);

        return kadaiEngineService.isCustomRoutingRulesEnabled().pipe(
          map((value) => {
            if (value) {
              return value;
            }
            return router.parseUrl('/kadai/workplace');
          }),
          catchError(() => {
            return of(router.parseUrl('/kadai/workplace'));
          })
        );
      }
    ],
    component: RoutingUploadComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TaskRoutingRoutingModule {}
