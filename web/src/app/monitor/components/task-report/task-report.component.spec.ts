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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TaskReportComponent } from './task-report.component';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

describe('TaskReportComponent', () => {
  let component: TaskReportComponent;
  let fixture: ComponentFixture<TaskReportComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskReportComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting(), provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskReportComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [],
      sumRow: [{ desc: ['Total'], cells: [10], total: 10, depth: 0, display: true }]
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component).toBeTruthy();
  });

  it('should not show report content when reportData is null — covers @if (reportData) false branch', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [],
      sumRow: [{ desc: ['Total'], cells: [10], total: 10, depth: 0, display: true }]
    };
    component.reportData = null;
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('.panel-default');
    expect(panel).toBeNull();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
  });

  it('should show report content after data is loaded — covers @if (reportData) true branch', () => {
    const mockReport = {
      meta: { header: ['H1', 'H2'], rowDesc: [], sumRowDesc: 'Total', name: 'Task Report', date: '2024-01-01' },
      rows: [{ desc: ['row1'], cells: [1, 2], total: 3, depth: 0, display: true }],
      sumRow: [{ desc: ['Total'], cells: [3], total: 3, depth: 0, display: true }]
    };
    component.reportData = mockReport as any;
    component.pieChartData = { labels: mockReport.meta.header, datasets: [{ data: [3] }] };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.reportData).toBeTruthy();
    expect(component.reportData.meta.name).toBe('Task Report');
    const panel = fixture.nativeElement.querySelector('.panel-default');
    expect(panel).toBeTruthy();
  });

  it('should populate pieChartData after HTTP response — covers chart data assignment in ngOnInit', () => {
    const mockReport = {
      meta: {
        header: ['READY', 'CLAIMED', 'COMPLETED'],
        rowDesc: [],
        sumRowDesc: 'Total',
        name: 'Task Report',
        date: '2024-01-01'
      },
      rows: [{ desc: ['Workbasket A'], cells: [1, 2, 3], total: 6, depth: 0, display: true }],
      sumRow: [{ desc: ['Total'], cells: [10, 20, 30], total: 60, depth: 0, display: true }]
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.pieChartData.labels).toEqual(['READY', 'CLAIMED', 'COMPLETED']);
    expect(component.pieChartData.datasets[0].data).toEqual([10, 20, 30]);
  });

  it('should have pieChartType set to pie', () => {
    const mockReport = {
      meta: { header: ['H1'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [],
      sumRow: [{ desc: ['Total'], cells: [5], total: 5, depth: 0, display: true }]
    };
    fixture.detectChanges();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.pieChartType).toBe('pie');
    expect(component.pieChartOptions.responsive).toBe(true);
    expect(component.pieChartOptions.maintainAspectRatio).toBe(true);
  });

  it('should not render panel heading before HTTP completes — @if false branch on init', () => {
    const mockReport = {
      meta: { header: ['H1'], rowDesc: [], sumRowDesc: 'Total', name: 'report', date: '2024' },
      rows: [],
      sumRow: [{ desc: ['Total'], cells: [5], total: 5, depth: 0, display: true }]
    };
    fixture.detectChanges();
    expect(component.reportData).toBeUndefined();
    const panelBefore = fixture.nativeElement.querySelector('.panel-default');
    expect(panelBefore).toBeNull();
    httpMock.match(() => true).forEach((req) => req.flush(mockReport));
    expect(component.reportData).toBeTruthy();
  });
});
