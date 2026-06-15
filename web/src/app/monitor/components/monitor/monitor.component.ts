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
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink, RouterOutlet } from '@angular/router';
import { MatTabLink, MatTabNav, MatTabNavPanel } from '@angular/material/tabs';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { Subject } from 'rxjs';
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
  domains = toSignal(inject(DomainService).getDomains(), { initialValue: [] });
  selectedDomain = toSignal(inject(DomainService).getSelectedDomain(), { initialValue: '' });
  destroy$ = new Subject<void>();
  private readonly domainService = inject(DomainService);

  ngOnInit(): void {
    this.selectedTab = 'tasks-priority';
  }

  switchDomain(domain: string) {
    this.domainService.switchDomain(domain);
  }
}
