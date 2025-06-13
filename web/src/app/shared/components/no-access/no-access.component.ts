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

import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';
import { MonitorRoles } from '../../roles/monitor.roles';
import { UserRoles } from '../../roles/user.roles';
import { BusinessAdminRoles } from '../../roles/business-admin.roles';

import { SvgIconComponent } from 'angular-svg-icon';

@Component({
  selector: 'kadai-shared-no-access',
  templateUrl: './no-access.component.html',
  styleUrls: ['./no-access.component.scss'],
  imports: [SvgIconComponent]
})
export class NoAccessComponent implements OnInit {
  router = inject(Router);
  showNoAccess = false;
  private kadaiEngineService = inject(KadaiEngineService);

  ngOnInit() {
    if (this.kadaiEngineService.hasRole(Object.values(BusinessAdminRoles))) {
      this.router.navigate(['administration']);
    } else if (this.kadaiEngineService.hasRole(Object.values(MonitorRoles))) {
      this.router.navigate(['monitor']);
    } else if (this.kadaiEngineService.hasRole(Object.values(UserRoles))) {
      this.router.navigate(['workplace']);
    } else {
      this.showNoAccess = true;
    }
  }
}
