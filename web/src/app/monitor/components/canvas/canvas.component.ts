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

import { AfterViewInit, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Chart, DoughnutController, ArcElement, Tooltip, Legend, Title } from 'chart.js';
import { ReportRow } from '../../models/report-row';
import { Select } from '@ngxs/store';
import { SettingsSelectors } from '../../../shared/store/settings-store/settings.selectors';
import { Observable, Subject } from 'rxjs';
import { Settings } from '../../../settings/models/settings';
import { takeUntil } from 'rxjs/operators';
import { SettingMembers } from '../../../settings/components/Settings/expected-members';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';

@Component({
  selector: 'kadai-monitor-canvas',
  templateUrl: './canvas.component.html',
  styleUrls: ['./canvas.component.scss'],
  providers: [provideCharts(withDefaultRegisterables())],
  standalone: true
})
export class CanvasComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() row: ReportRow;
  @Input() id: string;

  labels: string[];
  colors: string[];
  destroy$ = new Subject<void>();

  @Select(SettingsSelectors.getSettings) settings$: Observable<Settings>;

  ngOnInit() {
    this.settings$.pipe(takeUntil(this.destroy$)).subscribe((settings) => {
      this.setValuesFromSettings(settings);
    });
  }

  setValuesFromSettings(settings: Settings) {
    this.labels = [
      settings[SettingMembers.NameHighPriority],
      settings[SettingMembers.NameMediumPriority],
      settings[SettingMembers.NameLowPriority]
    ];
    this.colors = [
      settings[SettingMembers.ColorHighPriority],
      settings[SettingMembers.ColorMediumPriority],
      settings[SettingMembers.ColorLowPriority]
    ];
  }

  ngAfterViewInit() {
    const canvas = document.getElementById(this.id) as HTMLCanvasElement;
    if (canvas && this.id && this.row) {
      this.generateChart(this.id, this.row);
    }
  }

  generateChart(id: string, row: ReportRow) {
    const canvas = document.getElementById(id) as HTMLCanvasElement;
    new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: this.labels,
        datasets: [
          {
            label: 'Tasks by Priority',
            data: row.cells,
            backgroundColor: this.colors,
            borderWidth: 0
          }
        ]
      },
      options: {
        rotation: 270,
        circumference: 180,
        plugins: {
          title: {
            display: true,
            text: String(row.total),
            position: 'bottom',
            font: {
              size: 18
            }
          }
        }
      }
    });
  }

  ngOnDestroy() {
    document.getElementById(this.id).outerHTML = ''; // destroy HTML element
    this.destroy$.next();
    this.destroy$.complete();
  }

  constructor() {
    Chart.register(DoughnutController, ArcElement, Tooltip, Legend, Title);
  }
}
