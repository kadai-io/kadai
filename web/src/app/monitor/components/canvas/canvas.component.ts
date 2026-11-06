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

import { AfterViewInit, Component, inject, input, OnDestroy, OnInit, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ArcElement, Chart, DoughnutController, Legend, Title, Tooltip } from 'chart.js';
import { ReportRow } from 'app/monitor/models/report-row';
import { Store } from '@ngxs/store';
import { SettingsSelectors } from 'app/shared/store/settings-store/settings.selectors';
import { Observable } from 'rxjs';
import { Settings } from 'app/settings/models/settings';
import { SettingMembers } from 'app/settings/components/Settings/expected-members';

@Component({
  selector: 'kadai-monitor-canvas',
  templateUrl: './canvas.component.html',
  styleUrls: ['./canvas.component.scss']
})
export class CanvasComponent implements OnInit, AfterViewInit, OnDestroy {
  row = input<ReportRow>();
  id = input<string>();

  labels!: string[];
  colors!: string[];
  private destroyRef = inject(DestroyRef);

  settings$: Observable<Settings> = inject(Store).select(SettingsSelectors.getSettings);

  constructor() {
    Chart.register(DoughnutController, ArcElement, Tooltip, Legend, Title);
  }

  ngOnInit() {
    this.settings$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((settings) => {
      this.setValuesFromSettings(settings);
    });
  }

  setValuesFromSettings(settings: Settings) {
    this.labels = [
      settings[SettingMembers.NameHighPriority] as string,
      settings[SettingMembers.NameMediumPriority] as string,
      settings[SettingMembers.NameLowPriority] as string
    ];
    this.colors = [
      settings[SettingMembers.ColorHighPriority] as string,
      settings[SettingMembers.ColorMediumPriority] as string,
      settings[SettingMembers.ColorLowPriority] as string
    ];
  }

  ngAfterViewInit() {
    const canvas = document.getElementById(this.id()!) as HTMLCanvasElement;
    if (canvas && this.id() && this.row()) {
      this.generateChart(this.id()!, this.row()!);
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
    const element = document.getElementById(this.id()!);
    if (element) {
      element.outerHTML = '';
    }
  }
}
