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

import { Component, inject, Input, OnInit } from '@angular/core';
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
export class AdministrationOverviewComponent implements OnInit {
  @Input() selectedTab = '';
  domains: Array<string> = [];
  selectedDomain: string;
  destroy$ = new Subject<void>();
  routingAccess = false;
  private router = inject(Router);
  private domainService = inject(DomainService);
  private kadaiEngineService = inject(KadaiEngineService);

  constructor() {
    const router = this.router;

    router.events.pipe(takeUntil(this.destroy$)).subscribe((e) => {
      const urlPaths = this.router.url.split('/');
      if (this.router.url.includes('detail')) {
        this.selectedTab = urlPaths[urlPaths.length - 2];
      } else {
        this.selectedTab = urlPaths[urlPaths.length - 1];
      }
    });
  }

  ngOnInit() {
    this.kadaiEngineService
      .isCustomRoutingRulesEnabled()
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.routingAccess = value;
      });
    this.domainService
      .getDomains()
      .pipe(takeUntil(this.destroy$))
      .subscribe((domains) => {
        this.domains = domains;
      });

    this.domainService
      .getSelectedDomain()
      .pipe(takeUntil(this.destroy$))
      .subscribe((domain) => {
        this.selectedDomain = domain;
      });
  }

  switchDomain(domain: string) {
    this.domainService.switchDomain(domain);
  }
}
