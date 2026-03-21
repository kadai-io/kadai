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

import { ChangeDetectionStrategy, Component, inject, input, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { Subject } from 'rxjs';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { takeUntil } from 'rxjs/operators';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';
import { MatTabLink, MatTabNav, MatTabNavPanel } from '@angular/material/tabs';
import { MatFormField } from '@angular/material/form-field';
import { MatSelect } from '@angular/material/select';
import { MatTooltip } from '@angular/material/tooltip';
import { MatOption } from '@angular/material/core';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'kadai-administration-overview',
  templateUrl: './administration-overview.component.html',
  styleUrls: ['./administration-overview.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatTabNav,
    MatTabLink,
    RouterLink,
    MatFormField,
    MatSelect,
    MatTooltip,
    MatOption,
    MatTabNavPanel,
    RouterOutlet
  ]
})
export class AdministrationOverviewComponent implements OnInit {
  _selectedTabInput = input('', { alias: 'selectedTab' });
  selectedTab = signal('');
  routingAccess = toSignal(inject(KadaiEngineService).isCustomRoutingRulesEnabled(), { initialValue: false });
  domains = toSignal(inject(DomainService).getDomains(), { initialValue: [] as string[] });
  selectedDomain = toSignal(inject(DomainService).getSelectedDomain(), { initialValue: '' });
  destroy$ = new Subject<void>();
  private router = inject(Router);
  private domainService = inject(DomainService);

  constructor() {
    const router = this.router;

    router.events.pipe(takeUntil(this.destroy$)).subscribe((e) => {
      const urlPaths = this.router.url.split('/');
      if (this.router.url.includes('detail')) {
        this.selectedTab.set(urlPaths[urlPaths.length - 2]);
      } else {
        this.selectedTab.set(urlPaths[urlPaths.length - 1]);
      }
    });
  }

  ngOnInit() {
    this.selectedTab.set(this._selectedTabInput());
  }

  switchDomain(domain: string) {
    this.domainService.switchDomain(domain);
  }
}
