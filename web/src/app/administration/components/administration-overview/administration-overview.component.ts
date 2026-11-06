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

import { Component, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { DomainService } from 'app/shared/services/domain/domain.service';
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { MatTabLink, MatTabNav, MatTabNavPanel } from '@angular/material/tabs';
import { MatFormField } from '@angular/material/form-field';
import { MatSelect } from '@angular/material/select';
import { MatTooltip } from '@angular/material/tooltip';
import { MatOption } from '@angular/material/core';

@Component({
  selector: 'kadai-administration-overview',
  templateUrl: './administration-overview.component.html',
  styleUrls: ['./administration-overview.component.scss'],
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
export class AdministrationOverviewComponent {
  selectedTab = signal('');
  domains = signal<string[]>([]);
  selectedDomain = signal('');
  private destroyRef = inject(DestroyRef);
  routingAccess = signal(false);
  private router = inject(Router);
  private domainService = inject(DomainService);
  private kadaiEngineService = inject(KadaiEngineService);

  constructor() {
    const router = this.router;

    router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((e) => {
      const urlPaths = this.router.url.split('/');
      if (this.router.url.includes('detail')) {
        this.selectedTab.set(urlPaths[urlPaths.length - 2]);
      } else {
        this.selectedTab.set(urlPaths[urlPaths.length - 1]);
      }
    });

    this.kadaiEngineService
      .isCustomRoutingRulesEnabled()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.routingAccess.set(value);
      });

    this.domainService
      .getDomains()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((domains) => {
        this.domains.set(domains);
      });

    this.domainService
      .getSelectedDomain()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((domain) => {
        this.selectedDomain.set(domain);
      });
  }

  switchDomain(domain: string) {
    this.domainService.switchDomain(domain);
  }
}
