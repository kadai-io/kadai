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

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { WorkbasketType } from 'app/shared/models/workbasket-type';
import { SvgIconComponent } from 'angular-svg-icon';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'kadai-administration-icon-type',
  templateUrl: './icon-type.component.html',
  styleUrls: ['./icon-type.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SvgIconComponent, MatTooltip]
})
export class IconTypeComponent {
  type = input<WorkbasketType>();
  selected = input(false);
  tooltip = input(false);
  text = input<string>();
  size = input('small');

  iconSize = computed(() => (this.size() === 'large' ? '24' : '16'));
  iconColor = computed(() => (this.selected() ? 'white' : '#555'));

  getIconPath(type: WorkbasketType) {
    switch (type) {
      case WorkbasketType.PERSONAL:
        return 'user.svg';
      case WorkbasketType.GROUP:
        return 'users.svg';
      case WorkbasketType.TOPIC:
        return 'topic.svg';
      case WorkbasketType.CLEARANCE:
        return 'clearance.svg';
      default:
        return 'asterisk.svg';
    }
  }
}
