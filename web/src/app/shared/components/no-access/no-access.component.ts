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

import { Component, OnInit } from '@angular/core';
import { BusinessAdminGuard } from 'app/shared/guards/business-admin.guard';
import { MonitorGuard } from 'app/shared/guards/monitor.guard';
import { UserGuard } from 'app/shared/guards/user.guard';
import { Router } from '@angular/router';
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';

@Component({
  selector: 'kadai-shared-no-access',
  templateUrl: './no-access.component.html',
  styleUrls: ['./no-access.component.scss']
})
export class NoAccessComponent implements OnInit {
  showNoAccess = false;
  constructor(private kadaiEngineService: KadaiEngineService, public router: Router) {}

  ngOnInit() {
    if (this.kadaiEngineService.hasRole(BusinessAdminGuard.roles)) {
      this.router.navigate(['administration']);
    } else if (this.kadaiEngineService.hasRole(MonitorGuard.roles)) {
      this.router.navigate(['monitor']);
    } else if (this.kadaiEngineService.hasRole(UserGuard.roles)) {
      this.router.navigate(['workplace']);
    } else {
      this.showNoAccess = true;
    }
  }
}
