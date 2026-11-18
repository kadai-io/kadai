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

import { inject } from '@angular/core';
import { CanActivateFn, Routes, Router } from '@angular/router';

import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { Store } from '@ngxs/store';
import { FetchDomains } from '../shared/store/domain-store/domain.actions';
import { DOMAIN_QUERY_PARAM, MASTER_DOMAIN } from '../shared/store/domain-store/domain.state';
import { DomainSelectors } from '../shared/store/domain-store/domain.selectors';

const domainGuard: CanActivateFn = (route, state) => {
  const store = inject(Store);
  const router = inject(Router);

  return store.dispatch(new FetchDomains(false, route.queryParams[DOMAIN_QUERY_PARAM])).pipe(
    map(() => {
      if (state.url.includes('classifications')) {
        return true;
      }
      const requested = route.queryParams[DOMAIN_QUERY_PARAM] ?? store.selectSnapshot(DomainSelectors.selectedDomain);
      if (requested !== MASTER_DOMAIN) {
        return true;
      }
      const realDomain = store.selectSnapshot(DomainSelectors.domains).find((domain) => domain !== MASTER_DOMAIN);
      if (realDomain === undefined) {
        return true;
      }
      const target = router.parseUrl(state.url);
      target.queryParams = { ...target.queryParams, [DOMAIN_QUERY_PARAM]: realDomain };
      return target;
    }),
    catchError(() => {
      return of(false);
    })
  );
};

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./components/administration-overview/administration-overview.component').then(
        (m) => m.AdministrationOverviewComponent
      ),
    canActivate: [domainGuard],
    children: [
      {
        path: 'workbaskets',
        loadComponent: () =>
          import('./components/workbasket-overview/workbasket-overview.component').then(
            (m) => m.WorkbasketOverviewComponent
          ),
        canActivate: [domainGuard],
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./components/workbasket-overview/workbasket-overview.component').then(
                (m) => m.WorkbasketOverviewComponent
              ),
            outlet: 'master'
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./components/workbasket-overview/workbasket-overview.component').then(
                (m) => m.WorkbasketOverviewComponent
              ),
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
        loadComponent: () =>
          import('./components/classification-overview/classification-overview.component').then(
            (m) => m.ClassificationOverviewComponent
          ),
        canActivate: [domainGuard],
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./components/classification-overview/classification-overview.component').then(
                (m) => m.ClassificationOverviewComponent
              ),
            outlet: 'master'
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./components/classification-overview/classification-overview.component').then(
                (m) => m.ClassificationOverviewComponent
              ),
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
        loadComponent: () =>
          import('./components/access-items-management/access-items-management.component').then(
            (m) => m.AccessItemsManagementComponent
          ),
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
        loadChildren: () => import('@task-routing/task-routing.routes').then((taskRouting) => taskRouting.routes)
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
