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
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, NgClass } from '@angular/common';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';

import { MatDivider } from '@angular/material/divider';
import {
  MatTable,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatCellDef,
  MatCell,
  MatHeaderRowDef,
  MatHeaderRow,
  MatRowDef,
  MatRow
} from '@angular/material/table';
import { MatIcon } from '@angular/material/icon';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import { MatCheckbox } from '@angular/material/checkbox';

import { CanvasComponent } from '../canvas/canvas.component';
import { TaskPriorityReportFilterStateService } from '../../services/task-priority-report-filter-state.service';
import { TaskPriorityReportDataService } from '../../services/task-priority-report-data.service';
import { SettingMembers } from '../../../settings/components/Settings/expected-members';

@Component({
  selector: 'kadai-monitor-task-priority-report',
  templateUrl: './task-priority-report.component.html',
  styleUrls: ['./task-priority-report.component.scss'],
  imports: [
    MatDivider,
    CanvasComponent,
    MatTable,
    MatColumnDef,
    MatHeaderCellDef,
    MatHeaderCell,
    MatCellDef,
    MatCell,
    MatHeaderRowDef,
    MatHeaderRow,
    MatRowDef,
    MatRow,
    DatePipe,
    RouterLink,
    MatIcon,
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatCheckbox,
    NgClass
  ],
  host: {
    '[style.--color-high-priority]': 'colorHigh()',
    '[style.--color-medium-priority]': 'colorMedium()',
    '[style.--color-low-priority]': 'colorLow()'
  }
})
export class TaskPriorityReportComponent {
  private readonly activatedRoute = inject(ActivatedRoute);
  readonly filterState = inject(TaskPriorityReportFilterStateService);
  readonly dataService = inject(TaskPriorityReportDataService);

  readonly columns: string[] = ['priority', 'number'];
  isPanelOpen = false;

  readonly reportData = toSignal(this.dataService.reportData$);
  readonly keys = this.filterState.filterKeys;
  readonly filtersAreSpecified = this.filterState.filtersAreSpecified;
  readonly activeFilters = this.filterState.activeFilters;

  readonly isDepthZero = computed(() => this.filterState.workbasketKey() === undefined);

  readonly colorHigh = computed(() => this.filterState.settings()?.[SettingMembers.ColorHighPriority] ?? 'inherit');
  readonly colorMedium = computed(() => this.filterState.settings()?.[SettingMembers.ColorMediumPriority] ?? 'inherit');
  readonly colorLow = computed(() => this.filterState.settings()?.[SettingMembers.ColorLowPriority] ?? 'inherit');

  readonly tableDataArray = computed(() => {
    const report = this.reportData();
    const settings = this.filterState.settings();
    if (!report || !settings) return [];

    const nameHigh = settings[SettingMembers.NameHighPriority];
    const nameMedium = settings[SettingMembers.NameMediumPriority];
    const nameLow = settings[SettingMembers.NameLowPriority];

    return report.rows.map((row) => [
      { priority: nameHigh, number: row.cells[0] },
      { priority: nameMedium, number: row.cells[1] },
      { priority: nameLow, number: row.cells[2] },
      { priority: 'Total', number: row.total }
    ]);
  });

  constructor() {
    this.activatedRoute.params.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.filterState.workbasketKey.set(params['workbasketKey']);
    });
  }

  onFilterChange(isEnabled: boolean, key: string): void {
    this.filterState.toggleFilter(key, isEnabled);
  }

  indexToString(i: number): string {
    return String(i);
  }
}
