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
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';
import { SidenavService } from '../../services/sidenav/sidenav.service';
import { RequestInProgressService } from '../../services/request-in-progress/request-in-progress.service';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MonitorRoles } from '../../roles/monitor.roles';
import { UserRoles } from '../../roles/user.roles';
import { BusinessAdminRoles } from '../../roles/business-admin.roles';
import { MatListItem, MatNavList } from '@angular/material/list';

@Component({
  selector: 'kadai-sidenav-list',
  templateUrl: './sidenav-list.component.html',
  styleUrls: ['./sidenav-list.component.scss'],
  imports: [MatNavList, MatListItem, RouterLinkActive, RouterLink]
})
export class SidenavListComponent implements OnInit {
  toggle: boolean = false;
  monitorUrl = 'kadai/monitor';
  workplaceUrl = 'kadai/workplace';
  historyUrl = 'kadai/history';
  accessUrl = 'kadai/administration/access-items-management';
  routingUrl = 'kadai/administration/task-routing';
  classificationUrl = 'kadai/administration/classifications';
  workbasketsUrl = 'kadai/administration/workbaskets';
  administrationsUrl = 'kadai/administration/workbaskets';
  settingsURL = 'kadai/settings';
  administrationAccess = false;
  monitorAccess = false;
  workplaceAccess = false;
  historyAccess = false;
  routingAccess = false;
  settingsAccess = false;
  private kadaiEngineService = inject(KadaiEngineService);
  private sidenavService = inject(SidenavService);
  private requestInProgressService = inject(RequestInProgressService);
  private router = inject(Router);

  ngOnInit() {
    this.administrationAccess = this.kadaiEngineService.hasRole(Object.values(BusinessAdminRoles));
    this.monitorAccess = this.kadaiEngineService.hasRole(Object.values(MonitorRoles));
    this.workplaceAccess = this.kadaiEngineService.hasRole(Object.values(UserRoles));
    this.kadaiEngineService.isHistoryProviderEnabled().subscribe((value) => {
      this.historyAccess = value;
    });
    this.kadaiEngineService.isCustomRoutingRulesEnabled().subscribe((value) => {
      this.routingAccess = value;
    });
    this.settingsAccess = this.administrationAccess;
  }

  toggleSidenav(target: string) {
    if (!this.router.url.includes(target)) this.requestInProgressService.setRequestInProgress(true);
    this.toggle = !this.toggle;
    this.sidenavService.toggleSidenav();
  }
}
