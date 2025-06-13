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

import { AfterViewChecked, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ReportData } from '../../models/report-data';
import { MonitorService } from '../../services/monitor.service';
import { WorkbasketType } from '../../../shared/models/workbasket-type';
import { Select } from '@ngxs/store';
import { Observable, Subject } from 'rxjs';
import { SettingsSelectors } from '../../../shared/store/settings-store/settings.selectors';
import { Settings } from '../../../settings/models/settings';
import { mergeMap, take, takeUntil } from 'rxjs/operators';
import { SettingMembers } from '../../../settings/components/Settings/expected-members';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { TaskPriorityReportFilterComponent } from '../task-priority-report-filter/task-priority-report-filter.component';
import { MatDivider } from '@angular/material/divider';
import { CanvasComponent } from '../canvas/canvas.component';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable
} from '@angular/material/table';
import { DatePipe, NgClass } from '@angular/common';

@Component({
  selector: 'kadai-monitor-task-priority-report',
  templateUrl: './task-priority-report.component.html',
  styleUrls: ['./task-priority-report.component.scss'],
  imports: [
    TaskPriorityReportFilterComponent,
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
    NgClass
  ],
  providers: [MonitorService]
})
export class TaskPriorityReportComponent implements OnInit, AfterViewChecked, OnDestroy {
  columns: string[] = ['priority', 'number'];
  reportData: ReportData;
  tableDataArray: { priority: string; number: number }[][] = [];
  colorShouldChange = true;
  priority = [];
  nameHighPriority: string;
  nameMediumPriority: string;
  nameLowPriority: string;
  colorHighPriority: string;
  colorMediumPriority: string;
  colorLowPriority: string;
  destroy$ = new Subject<void>();
  @Select(SettingsSelectors.getSettings)
  settings$: Observable<Settings>;
  private monitorService = inject(MonitorService);
  private requestInProgressService = inject(RequestInProgressService);

  ngOnInit() {
    this.requestInProgressService.setRequestInProgress(true);
    this.settings$
      .pipe(
        takeUntil(this.destroy$),
        mergeMap((settings) => {
          this.setValuesFromSettings(settings);
          // the order must be high, medium, low because the canvas component defines its labels in this order
          this.priority = [
            settings[SettingMembers.IntervalHighPriority],
            settings[SettingMembers.IntervalMediumPriority],
            settings[SettingMembers.IntervalLowPriority]
          ].map((arr) => ({ lowerBound: arr[0], upperBound: arr[1] }));
          return this.monitorService.getTasksByPriorityReport([WorkbasketType.TOPIC], this.priority);
        })
      )
      .subscribe((reportData) => {
        this.setValuesFromReportData(reportData);
        this.requestInProgressService.setRequestInProgress(false);
      });
  }

  ngAfterViewChecked() {
    if (this.colorShouldChange) {
      const highPriorityElements = document.getElementsByClassName('task-priority-report__row--high');
      if (highPriorityElements.length > 0) {
        this.colorShouldChange = false;
        this.changeColor();
      }
    }
  }

  setValuesFromSettings(settings: Settings) {
    this.nameHighPriority = settings[SettingMembers.NameHighPriority];
    this.nameMediumPriority = settings[SettingMembers.NameMediumPriority];
    this.nameLowPriority = settings[SettingMembers.NameLowPriority];
    this.colorHighPriority = settings[SettingMembers.ColorHighPriority];
    this.colorMediumPriority = settings[SettingMembers.ColorMediumPriority];
    this.colorLowPriority = settings[SettingMembers.ColorLowPriority];
  }

  setValuesFromReportData(reportData) {
    this.reportData = reportData;

    // the order must be high, medium, low because the canvas component defines its labels in this order
    let indexHigh = 0;
    let indexMedium = 1;
    let indexLow = 2;

    this.tableDataArray = [];
    reportData.rows.forEach((row) => {
      this.tableDataArray.push([
        { priority: this.nameHighPriority, number: row.cells[indexHigh] },
        { priority: this.nameMediumPriority, number: row.cells[indexMedium] },
        { priority: this.nameLowPriority, number: row.cells[indexLow] },
        { priority: 'Total', number: row.total }
      ]);
    });
  }

  changeColor() {
    const highPriorityElements = document.getElementsByClassName('task-priority-report__row--high');
    const mediumPriorityElements = document.getElementsByClassName('task-priority-report__row--medium');
    const lowPriorityElements = document.getElementsByClassName('task-priority-report__row--low');
    this.applyColorOnClasses(highPriorityElements, this.colorHighPriority);
    this.applyColorOnClasses(mediumPriorityElements, this.colorMediumPriority);
    this.applyColorOnClasses(lowPriorityElements, this.colorLowPriority);
  }

  applyColorOnClasses(elements: HTMLCollectionOf<Element>, color: string) {
    for (let i = 0; i < elements.length; i++) {
      (<HTMLElement>elements[i]).style.color = color;
    }
  }

  indexToString(i: number): string {
    return String(i);
  }

  applyFilter(filter: {}) {
    this.requestInProgressService.setRequestInProgress(true);
    this.monitorService
      .getTasksByPriorityReport([WorkbasketType.TOPIC], this.priority, filter)
      .pipe(take(1))
      .subscribe((reportData) => {
        this.colorShouldChange = true;
        this.setValuesFromReportData(reportData);
        this.requestInProgressService.setRequestInProgress(false);
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
