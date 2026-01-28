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

import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ALL_STATES, TaskState } from '../../models/task-state';
import { TaskQueryFilterParameter } from '../../models/task-query-filter-parameter';
import { Actions, ofActionCompleted, Store } from '@ngxs/store';
import { ClearTaskFilter, SetTaskFilter } from '../../store/filter-store/filter.actions';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatTooltip } from '@angular/material/tooltip';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatSelect } from '@angular/material/select';

import { MatOption } from '@angular/material/core';
import { MapValuesPipe } from '../../pipes/map-values.pipe';

@Component({
  selector: 'kadai-shared-task-filter',
  templateUrl: './task-filter.component.html',
  styleUrls: ['./task-filter.component.scss'],
  imports: [MatFormField, MatTooltip, MatLabel, MatInput, FormsModule, MatSelect, MatOption, MapValuesPipe]
})
export class TaskFilterComponent implements OnInit, OnDestroy {
  filter: TaskQueryFilterParameter;
  destroy$ = new Subject<void>();
  allStates: Map<TaskState, string> = ALL_STATES;
  private store = inject(Store);
  private ngxsActions$ = inject(Actions);

  ngOnInit() {
    this.clear();
    this.ngxsActions$.pipe(ofActionCompleted(ClearTaskFilter), takeUntil(this.destroy$)).subscribe(() => this.clear());
  }

  setStatus(state: TaskState) {
    this.filter.state = state !== TaskState.ALL ? [state] : [];
    this.updateState();
  }

  // TODO: filter tasks when pressing 'enter'
  search() {}

  updateState() {
    this.store.dispatch(new SetTaskFilter(this.filter));
  }

  clear() {
    this.filter = {
      priority: [],
      'name-like': [],
      'owner-like': []
    };
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
