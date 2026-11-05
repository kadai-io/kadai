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

import { Component, computed, inject } from '@angular/core';
import { KadaiEngineService } from 'app/shared/services/kadai-engine/kadai-engine.service';
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
export class UserInformationComponent {
  private kadaiEngineService = inject(KadaiEngineService);

  readonly userInformation = this.kadaiEngineService.currentUserInfo;
  readonly roles = computed(() => {
    const user = this.userInformation();
    return user?.roles ? `[${user.roles.join(',')}]` : '';
  });

  showRoles = false;

  toggleRoles(): void {
    this.showRoles = !this.showRoles;
  }
}
