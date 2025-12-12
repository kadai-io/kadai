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

import { AfterViewChecked, Component, effect, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ReportData } from '../../models/report-data';
import { MonitorService } from '../../services/monitor.service';
import { WorkbasketType } from '../../../shared/models/workbasket-type';
import { Store } from '@ngxs/store';
import { Observable, Subject } from 'rxjs';
import { SettingsSelectors } from '../../../shared/store/settings-store/settings.selectors';
import { Settings } from '../../../settings/models/settings';
import { take, takeUntil } from 'rxjs/operators';
import { SettingMembers } from '../../../settings/components/Settings/expected-members';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
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
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { toSignal } from '@angular/core/rxjs-interop';
import { DomainService } from '../../../shared/services/domain/domain.service';
import {
  MatAccordion,
  MatExpansionPanel,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import { MatCheckbox } from '@angular/material/checkbox';
import { TaskPriorityReportFilterStateService } from '../../services/task-priority-report-filter-state.service';

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
    NgClass,
    RouterLink,
    MatIcon,
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatCheckbox
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
  settings$: Observable<Settings> = inject(Store).select(SettingsSelectors.getSettings);
  workbasketKey = signal<string>(undefined);
  isPanelOpen = false;
  filters: {}[];
  keys: string[];
  filtersAreSpecified = false;
  private readonly monitorService = inject(MonitorService);
  private readonly requestInProgressService = inject(RequestInProgressService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly domainService = inject(DomainService);
  private readonly filterState = inject(TaskPriorityReportFilterStateService);
  readonly activeFilters = this.filterState.activeFilters;
  private readonly domain = toSignal(this.domainService.getSelectedDomain(), {
    initialValue: this.domainService.getSelectedDomainValue?.()
  });
  private readonly settings = toSignal(this.settings$, { initialValue: undefined as Settings });
  private readonly currentFilter = this.filterState.currentFilter;

  constructor() {
    this.activatedRoute.params.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      this.workbasketKey.set(params['workbasketKey']);
    });

    effect((onCleanup) => {
      const settings = this.settings();
      const domain = this.domain();
      const filter = this.currentFilter();

      this.requestInProgressService.setRequestInProgress(true);
      this.setValuesFromSettings(settings);
      // the order must be high, medium, low because the canvas component defines its labels in this order
      this.priority = [
        settings[SettingMembers.IntervalHighPriority],
        settings[SettingMembers.IntervalMediumPriority],
        settings[SettingMembers.IntervalLowPriority]
      ].map((arr) => ({ lowerBound: arr[0], upperBound: arr[1] }));

      const reportData = this.isDepthZero()
        ? this.monitorService.getTasksByPriorityReport([WorkbasketType.TOPIC], this.priority, domain, filter)
        : this.monitorService.getTasksByDetailedPriorityReport([WorkbasketType.TOPIC], this.priority, domain, filter);

      const reportDataSubscription = reportData.subscribe({
        next: (reportData) => {
          this.colorShouldChange = true;
          this.setValuesFromReportData(reportData);
          this.requestInProgressService.setRequestInProgress(false);
        },
        error: (err) => {
          console.error('Failed to load Task Priority Report', err);
          this.requestInProgressService.setRequestInProgress(false);
        }
      });

      onCleanup(() => reportDataSubscription.unsubscribe());
    });
  }

  ngOnInit(): void {
    this.settings$.pipe(takeUntil(this.destroy$)).subscribe((settings) => {
      this.filtersAreSpecified = !!settings?.['filter'] && settings['filter'] !== '';
      if (this.filtersAreSpecified) {
        try {
          this.filters = JSON.parse(settings['filter']);
          this.keys = Object.keys(this.filters as any);
          this.rebuildActiveFiltersFromCurrentFilter();
        } catch {
          this.filters = [] as any;
          this.keys = [];
          this.filtersAreSpecified = false;
        }
      } else {
        this.filters = [] as any;
        this.keys = [];
      }
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

  setValuesFromReportData(reportData: ReportData) {
    const depth = this.isDepthZero() ? 0 : 1;
    this.reportData = {
      meta: reportData.meta,
      rows: reportData.rows
        .filter((row) => row.depth === depth)
        .filter((row) => this.isDepthZero() || row.desc[0] === this.workbasketKey()),
      sumRow: reportData.sumRow
    };

    // the order must be high, medium, low because the canvas component defines its labels in this order
    let indexHigh = 0;
    let indexMedium = 1;
    let indexLow = 2;

    this.tableDataArray = [];
    this.reportData.rows.forEach((row) => {
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
    this.currentFilter.set(filter);
    this.requestInProgressService.setRequestInProgress(true);

    if (this.isDepthZero()) {
      this.monitorService
        .getTasksByPriorityReport([WorkbasketType.TOPIC], this.priority, this.domain(), filter)
        .pipe(take(1))
        .subscribe((reportData) => {
          this.setValuesFromReportData(reportData);
          this.requestInProgressService.setRequestInProgress(false);
        });
    } else {
      this.monitorService
        .getTasksByDetailedPriorityReport([WorkbasketType.TOPIC], this.priority, this.domain(), filter)
        .pipe(take(1))
        .subscribe((reportData) => {
          this.setValuesFromReportData(reportData);
          this.requestInProgressService.setRequestInProgress(false);
        });
    }
  }

  emitFilter(isEnabled: boolean, key: string) {
    const next = isEnabled ? [...this.activeFilters(), key] : this.activeFilters().filter((element) => element !== key);
    this.activeFilters.set(next);
    this.applyFilter(this.buildQuery());
  }

  buildQuery(): {} {
    const filterQuery: any = {};
    (this.activeFilters() || []).forEach((activeFilter) => {
      const filter: any = (this.filters as any)[activeFilter];
      if (!filter) return;
      const keys = Object.keys(filter);
      keys.forEach((k) => {
        const newValue = filter[k];
        filterQuery[k] = filterQuery[k] ? [...filterQuery[k], ...newValue] : newValue;
      });
    });
    return filterQuery;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected isDepthZero() {
    return this.workbasketKey() === undefined;
  }

  private rebuildActiveFiltersFromCurrentFilter() {
    const cfg: any = this.filters as any;
    if (!cfg) {
      this.activeFilters.set([]);
      return;
    }
    const curr: any = this.currentFilter();
    const next: string[] = [];
    Object.keys(cfg).forEach((key) => {
      const defs = cfg[key];
      const allMatch = Object.keys(defs || {}).every((k) => {
        const need: any[] = defs[k] || [];
        const have: any[] = curr?.[k] || [];
        return need.every((v) => have.includes(v));
      });
      if (allMatch) {
        next.push(key);
      }
    });
    this.activeFilters.set(next);
  }
}
