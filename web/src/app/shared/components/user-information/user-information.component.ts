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
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { UserInfo } from 'app/shared/models/user-info';
import { expandDown } from '../../animations/expand.animation';
import { SvgIconComponent } from 'angular-svg-icon';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'kadai-shared-user-information',
  templateUrl: './user-information.component.html',
  styleUrls: ['./user-information.component.scss'],
  animations: [expandDown],
  imports: [SvgIconComponent, MatButton]
})
export class UserInformationComponent implements OnInit {
  userInformation: UserInfo;
  roles = '';
  showRoles = false;
  private kadaiEngineService = inject(KadaiEngineService);

  ngOnInit() {
    this.userInformation = this.kadaiEngineService.currentUserInfo;
    if (this.userInformation) {
      this.roles = `[${this.userInformation.roles.join(',')}]`;
    }
  }

  toggleRoles() {
    this.showRoles = !this.showRoles;
  }
}
