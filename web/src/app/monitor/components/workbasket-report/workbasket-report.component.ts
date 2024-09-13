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

import { Component, OnInit } from '@angular/core';
import { MetaInfoData } from '../../models/meta-info-data';
enum WorkbasketReports {
  DUE_DATE,
  PLANNED_DATE
}

@Component({
  selector: 'kadai-monitor-workbasket-report',
  templateUrl: './workbasket-report.component.html',
  styleUrls: ['./workbasket-report.component.scss']
})
export class WorkbasketReportComponent implements OnInit {
  metaInformation: MetaInfoData;
  selectedComponent: WorkbasketReports;

  ngOnInit() {}

  getMetaInformation(metaInformation: MetaInfoData) {
    this.metaInformation = metaInformation;
  }

  selectComponent(component) {
    this.selectedComponent = component;
  }
  getTitle(): string {
    return this.selectedComponent
      ? 'Tasks grouped by Workbasket, querying by planned date'
      : 'Tasks grouped by Workbasket, querying by due date';
  }
}
