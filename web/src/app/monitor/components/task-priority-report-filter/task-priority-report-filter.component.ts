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

import { Component, EventEmitter, inject, OnDestroy, OnInit, Output } from '@angular/core';
import { Store } from '@ngxs/store';
import { Observable, Subject } from 'rxjs';
import { SettingsSelectors } from '../../../shared/store/settings-store/settings.selectors';
import { Settings } from '../../../settings/models/settings';
import { takeUntil } from 'rxjs/operators';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';

import { MatCheckbox } from '@angular/material/checkbox';

@Component({
  selector: 'kadai-monitor-task-priority-report-filter',
  templateUrl: './task-priority-report-filter.component.html',
  styleUrls: ['./task-priority-report-filter.component.scss'],
  imports: [MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle, MatCheckbox]
})
export class TaskPriorityReportFilterComponent implements OnInit, OnDestroy {
  isPanelOpen = false;
  filters: {}[];
  keys: string[];
  activeFilters = [];
  filtersAreSpecified: boolean = true;
  destroy$ = new Subject<void>();

  @Output() applyFilter = new EventEmitter<Object>();

  settings$: Observable<Settings> = inject(Store).select(SettingsSelectors.getSettings);

  ngOnInit() {
    this.settings$.pipe(takeUntil(this.destroy$)).subscribe((settings) => {
      this.filtersAreSpecified = settings['filter'] && settings['filter'] !== '';
      if (this.filtersAreSpecified) {
        this.filters = JSON.parse(settings['filter']);
        this.keys = Object.keys(this.filters);
      }
    });
  }

  emitFilter(isEnabled: boolean, key: string) {
    this.activeFilters = isEnabled
      ? [...this.activeFilters, key]
      : this.activeFilters.filter((element) => element !== key);

    this.applyFilter.emit(this.buildQuery());
  }

  buildQuery(): {} {
    let filterQuery = {};
    this.activeFilters.forEach((activeFilter) => {
      const filter = this.filters[activeFilter];
      const keys = Object.keys(filter);
      keys.forEach((key) => {
        const newValue = filter[key];
        filterQuery[key] = filterQuery[key] ? [...filterQuery[key], ...newValue] : newValue;
      });
    });
    return filterQuery;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
