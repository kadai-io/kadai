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

import { Component, EventEmitter, inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Task } from 'app/workplace/models/task';
import { takeUntil } from 'rxjs/operators';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { Subject } from 'rxjs';

import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'kadai-task-custom-fields',
  templateUrl: './task-custom-fields.component.html',
  styleUrls: ['./task-custom-fields.component.scss'],
  imports: [MatFormField, MatLabel, MatInput, FormsModule]
})
export class TaskCustomFieldsComponent implements OnInit, OnDestroy {
  @Input() task: Task;
  @Output() taskChange: EventEmitter<Task> = new EventEmitter<Task>();
  readonly lengthError = 'You have reached the maximum length';
  inputOverflowMap = new Map<string, boolean>();
  validateKeypress: Function;
  customFields: string[];
  destroy$ = new Subject<void>();
  private formsValidatorService = inject(FormsValidatorService);

  ngOnInit() {
    this.formsValidatorService.inputOverflowObservable.pipe(takeUntil(this.destroy$)).subscribe((inputOverflowMap) => {
      this.inputOverflowMap = inputOverflowMap;
    });
    this.validateKeypress = (inputFieldModel, maxLength) => {
      this.formsValidatorService.validateInputOverflow(inputFieldModel, maxLength);
    };

    this.customFields = Object.keys(this.task).filter(
      (attribute) => attribute.startsWith('custom') && /\d/.test(attribute)
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
