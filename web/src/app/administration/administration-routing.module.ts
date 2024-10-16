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
import { RouterModule, Routes } from '@angular/router';

import { AccessItemsManagementComponent } from './components/access-items-management/access-items-management.component';
import { ClassificationOverviewComponent } from './components/classification-overview/classification-overview.component';
import { WorkbasketOverviewComponent } from './components/workbasket-overview/workbasket-overview.component';
import { AdministrationOverviewComponent } from './components/administration-overview/administration-overview.component';
import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { DomainService } from '../shared/services/domain/domain.service';

const domainGuard = () => {
  const domainService = inject(DomainService);

  return domainService.getDomains().pipe(
    map(() => true),
    catchError(() => {
      return of(false);
    })
  );
};

const routes: Routes = [
  {
    path: '',
    component: AdministrationOverviewComponent,
    canActivate: [domainGuard],
    children: [
      {
        path: 'workbaskets',
        component: WorkbasketOverviewComponent,
        canActivate: [domainGuard],
        children: [
          {
            path: '',
            component: WorkbasketOverviewComponent,
            outlet: 'master'
          },
          {
            path: ':id',
            component: WorkbasketOverviewComponent,
            outlet: 'detail'
          },
          {
            path: '**',
            redirectTo: ''
          }
        ]
      },
      {
        path: 'classifications',
        component: ClassificationOverviewComponent,
        canActivate: [domainGuard],
        children: [
          {
            path: '',
            component: ClassificationOverviewComponent,
            outlet: 'master'
          },
          {
            path: ':id',
            component: ClassificationOverviewComponent,
            outlet: 'detail'
          },
          {
            path: '**',
            redirectTo: ''
          }
        ]
      },
      {
        path: 'access-items-management',
        component: AccessItemsManagementComponent,
        canActivate: [domainGuard],
        children: [
          {
            path: '**',
            redirectTo: ''
          }
        ]
      },
      {
        path: 'task-routing',
        canActivate: [domainGuard],
        loadChildren: () => import('@task-routing/task-routing.module').then((m) => m.TaskRoutingModule)
      }
    ]
  },
  {
    path: '',
    redirectTo: '',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdministrationRoutingModule {}
