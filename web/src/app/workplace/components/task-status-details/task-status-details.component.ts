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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Task } from 'app/workplace/models/task';
import { DatePipe } from '@angular/common';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'kadai-task-status-details',
  templateUrl: './task-status-details.component.html',
  styleUrls: ['./task-status-details.component.scss'],
  imports: [MatFormField, MatLabel, MatInput, FormsModule, DatePipe]
})
export class TaskStatusDetailsComponent {
  @Input() task: Task;
  @Output() taskChange: EventEmitter<Task> = new EventEmitter<Task>();
}
