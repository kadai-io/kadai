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

import { Component, Input } from '@angular/core';
import { CustomAttribute } from 'app/workplace/models/task';

import { MatDivider } from '@angular/material/divider';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'kadai-task-attribute-value',
  templateUrl: './task-attribute-value.component.html',
  styleUrls: ['./task-attribute-value.component.scss'],
  imports: [MatDivider, MatButton, MatTooltip, MatIcon, MatFormField, MatInput, FormsModule]
})
export class TaskAttributeValueComponent {
  @Input() callbackInfo = false;
  @Input() attributes: CustomAttribute[] = [];

  addAttribute(): void {
    this.attributes.push({ key: '', value: '' });
  }

  removeAttribute(idx: number): void {
    this.attributes.splice(idx, 1);
  }
}
