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
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
import { UserInfo } from 'app/shared/models/user-info';
import { expandDown } from '../../animations/expand.animation';

@Component({
  selector: 'kadai-shared-user-information',
  templateUrl: './user-information.component.html',
  styleUrls: ['./user-information.component.scss'],
  animations: [expandDown]
})
export class UserInformationComponent implements OnInit {
  userInformation: UserInfo;
  roles = '';
  showRoles = false;
  constructor(private kadaiEngineService: KadaiEngineService) {}

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
