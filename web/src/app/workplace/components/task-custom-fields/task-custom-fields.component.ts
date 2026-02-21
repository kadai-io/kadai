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

import { ChangeDetectionStrategy, Component, inject, model, OnInit } from '@angular/core';
import { Task } from 'app/workplace/models/task';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { toSignal } from '@angular/core/rxjs-interop';

import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'kadai-task-custom-fields',
  templateUrl: './task-custom-fields.component.html',
  styleUrls: ['./task-custom-fields.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatFormField, MatLabel, MatInput, FormsModule]
})
export class TaskCustomFieldsComponent implements OnInit {
  task = model<Task>();
  readonly lengthError = 'You have reached the maximum length';
  inputOverflowMap = toSignal(inject(FormsValidatorService).inputOverflowObservable, {
    initialValue: new Map<string, boolean>()
  });
  validateKeypress: Function;
  customFields: string[];
  private formsValidatorService = inject(FormsValidatorService);

  ngOnInit() {
    this.validateKeypress = (inputFieldModel, maxLength) => {
      this.formsValidatorService.validateInputOverflow(inputFieldModel, maxLength);
    };

    this.customFields = Object.keys(this.task()).filter(
      (attribute) => attribute.startsWith('custom') && /\d/.test(attribute)
    );
  }
}
