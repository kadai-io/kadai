/*
 * Copyright [2024] [envite consulting GmbH]
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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AlertModule } from 'ngx-bootstrap/alert';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { NgChartsModule } from 'ng2-charts';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { MapToIterable } from 'app/shared/pipes/map-to-iterable.pipe';
import { SharedModule } from '../shared/shared.module';
import { MonitorRoutingModule } from './monitor-routing.module';

/**
 * Components
 */
import { ReportTableComponent } from './components/report-table/report-table.component';
import { MonitorComponent } from './components/monitor/monitor.component';
import { TaskReportComponent } from './components/task-report/task-report.component';
import { ClassificationReportComponent } from './components/classification-report/classification-report.component';
import { TimestampReportComponent } from './components/timestamp-report/timestamp-report.component';
import { WorkbasketReportComponent } from './components/workbasket-report/workbasket-report.component';
import { WorkbasketReportPlannedDateComponent } from './components/workbasket-report-planned-date/workbasket-report-planned-date.component';
import { WorkbasketReportDueDateComponent } from './components/workbasket-report-due-date/workbasket-report-due-date.component';
import { TaskPriorityReportComponent } from './components/task-priority-report/task-priority-report.component';
import { CanvasComponent } from './components/canvas/canvas.component';
import { TaskPriorityReportFilterComponent } from './components/task-priority-report-filter/task-priority-report-filter.component';

/**
 * Services
 */
import { MonitorService } from './services/monitor.service';

/**
 * Material Design
 */
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { provideHttpClient } from '@angular/common/http';

const MODULES = [
  CommonModule,
  MonitorRoutingModule,
  FormsModule,
  AlertModule.forRoot(),
  NgChartsModule,
  TabsModule.forRoot(),
  AngularSvgIconModule,
  SharedModule,
  MatTabsModule,
  MatButtonModule,
  MatTableModule,
  MatExpansionModule,
  MatCheckboxModule,
  MatDividerModule
];
const DECLARATIONS = [
  ReportTableComponent,
  MonitorComponent,
  TaskPriorityReportComponent,
  TaskPriorityReportFilterComponent,
  CanvasComponent,
  TimestampReportComponent,
  WorkbasketReportComponent,
  WorkbasketReportPlannedDateComponent,
  WorkbasketReportDueDateComponent,
  TaskReportComponent,
  ClassificationReportComponent
];

@NgModule({
  declarations: DECLARATIONS,
  imports: [MODULES],
  providers: [MonitorService, MapToIterable, provideHttpClient()]
})
export class MonitorModule {}
