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

import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatTabLink, MatTabNav, MatTabNavPanel } from '@angular/material/tabs';
import { Store } from '@ngxs/store';
import { DomainSelectors } from 'app/shared/store/domain-store/domain.selectors';
import { FetchDomains } from 'app/shared/store/domain-store/domain.actions';
import { DOMAIN_QUERY_PARAM } from 'app/shared/store/domain-store/domain.state';
import { MatFormField } from '@angular/material/form-field';
import { MatSelect } from '@angular/material/select';
import { MatTooltip } from '@angular/material/tooltip';
import { MatOption } from '@angular/material/core';

@Component({
  selector: 'kadai-monitor',
  templateUrl: './monitor.component.html',
  styleUrls: ['./monitor.component.scss'],
  imports: [
    MatTabNav,
    MatTabLink,
    RouterLink,
    MatTabNavPanel,
    RouterOutlet,
    MatFormField,
    MatSelect,
    MatOption,
    MatTooltip
  ]
})
export class MonitorComponent implements OnInit {
  selectedTab = '';
  private readonly store = inject(Store);
  private router = inject(Router);
  domains = this.store.selectSignal(DomainSelectors.domains);
  selectedDomain = this.store.selectSignal(DomainSelectors.selectedDomain);

  ngOnInit(): void {
    this.selectedTab = 'tasks-priority';
    this.store.dispatch(new FetchDomains());
  }

  switchDomain(domain: string) {
    this.router.navigate([], {
      queryParams: { [DOMAIN_QUERY_PARAM]: domain },
      queryParamsHandling: 'merge'
    });
  }
}
