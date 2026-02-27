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

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { MonitorService } from './monitor.service';
import { environment } from 'environments/environment';
import { ReportData } from '../models/report-data';

describe('MonitorService', () => {
  let service: MonitorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    environment.kadaiRestUrl = 'http://test';

    TestBed.configureTestingModule({
      providers: [MonitorService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(MonitorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getTaskStatusReport', () => {
    it('should make a GET request to task-status-report with state query params', () => {
      service.getTaskStatusReport().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('task-status-report'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('/v1/monitor/task-status-report');
      req.flush({});
    });

    it('should include READY, CLAIMED, and COMPLETED states in query', () => {
      service.getTaskStatusReport().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('task-status-report'));
      expect(req.request.urlWithParams).toContain('states=READY');
      expect(req.request.urlWithParams).toContain('states=CLAIMED');
      expect(req.request.urlWithParams).toContain('states=COMPLETED');
      req.flush({});
    });
  });

  describe('getWorkbasketStatisticsQueryingByDueDate', () => {
    it('should make a GET request to workbasket-report with state query params', () => {
      service.getWorkbasketStatisticsQueryingByDueDate().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('workbasket-report'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('/v1/monitor/workbasket-report');
      req.flush({});
    });

    it('should include state params but not task-timetamp param', () => {
      service.getWorkbasketStatisticsQueryingByDueDate().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('workbasket-report'));
      expect(req.request.urlWithParams).toContain('states=READY');
      expect(req.request.urlWithParams).not.toContain('task-timetamp');
      req.flush({});
    });
  });

  describe('getWorkbasketStatisticsQueryingByPlannedDate', () => {
    it('should make a GET request to workbasket-report', () => {
      service.getWorkbasketStatisticsQueryingByPlannedDate().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('workbasket-report'));
      expect(req.request.method).toBe('GET');
      req.flush({});
    });

    it('should include task-timetamp=PLANNED param', () => {
      service.getWorkbasketStatisticsQueryingByPlannedDate().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('workbasket-report'));
      expect(req.request.urlWithParams).toContain('task-timetamp=PLANNED');
      req.flush({});
    });
  });

  describe('getClassificationTasksReport', () => {
    it('should make a GET request to classification-report', () => {
      service.getClassificationTasksReport().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('classification-report'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('/v1/monitor/classification-report');
      req.flush({});
    });
  });

  describe('getDailyEntryExitReport', () => {
    it('should make a GET request to timestamp-report', () => {
      service.getDailyEntryExitReport().subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('timestamp-report'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('/v1/monitor/timestamp-report');
      req.flush({});
    });
  });

  describe('getChartData', () => {
    it('should map report rows to ChartData objects', () => {
      const reportData: ReportData = {
        meta: null,
        rows: [{ desc: ['label1'], cells: [1, 2, 3], total: 6, depth: 0, display: true }],
        sumRow: []
      };

      const result = service.getChartData(reportData);

      expect(result).toHaveLength(1);
      expect(result[0].label).toBe('label1');
      expect(result[0].data).toEqual([1, 2, 3]);
    });

    it('should map multiple rows correctly', () => {
      const reportData: ReportData = {
        meta: null,
        rows: [
          { desc: ['row1'], cells: [10, 20], total: 30, depth: 0, display: true },
          { desc: ['row2'], cells: [5, 15], total: 20, depth: 0, display: true }
        ],
        sumRow: []
      };

      const result = service.getChartData(reportData);

      expect(result).toHaveLength(2);
      expect(result[0].label).toBe('row1');
      expect(result[0].data).toEqual([10, 20]);
      expect(result[1].label).toBe('row2');
      expect(result[1].data).toEqual([5, 15]);
    });

    it('should return empty array when rows is empty', () => {
      const reportData: ReportData = { meta: null, rows: [], sumRow: [] };
      const result = service.getChartData(reportData);
      expect(result).toEqual([]);
    });

    it('should use first element of desc as label', () => {
      const reportData: ReportData = {
        meta: null,
        rows: [{ desc: ['first', 'second', 'third'], cells: [1], total: 1, depth: 0, display: true }],
        sumRow: []
      };

      const result = service.getChartData(reportData);
      expect(result[0].label).toBe('first');
    });
  });

  describe('getTasksByPriorityReport', () => {
    it('should make a GET request to workbasket-priority-report', () => {
      service.getTasksByPriorityReport(['PERSONAL'], [1, 2], 'DOMAIN_A').subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('workbasket-priority-report'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('/v1/monitor/workbasket-priority-report');
      req.flush({});
    });

    it('should include workbasket-type, domain, state, and columnHeader params', () => {
      service.getTasksByPriorityReport(['GROUP'], [5, 10], 'DOMAIN_B').subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('workbasket-priority-report'));
      expect(req.request.urlWithParams).toContain('workbasket-type=GROUP');
      expect(req.request.urlWithParams).toContain('domain=DOMAIN_B');
      expect(req.request.urlWithParams).toContain('state=READY');
      req.flush({});
    });

    it('should merge custom filters into query params', () => {
      service.getTasksByPriorityReport(['PERSONAL'], [1], 'DOMAIN_A', { custom: 'value' }).subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('workbasket-priority-report'));
      expect(req.request.urlWithParams).toContain('custom=value');
      req.flush({});
    });
  });

  describe('getTasksByDetailedPriorityReport', () => {
    it('should make a GET request to detailed-workbasket-priority-report', () => {
      service.getTasksByDetailedPriorityReport(['PERSONAL'], [1, 2], 'DOMAIN_A').subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('detailed-workbasket-priority-report'));
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toContain('/v1/monitor/detailed-workbasket-priority-report');
      req.flush({});
    });

    it('should include required query params', () => {
      service.getTasksByDetailedPriorityReport(['GROUP'], [3], 'DOMAIN_C').subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('detailed-workbasket-priority-report'));
      expect(req.request.urlWithParams).toContain('workbasket-type=GROUP');
      expect(req.request.urlWithParams).toContain('domain=DOMAIN_C');
      expect(req.request.urlWithParams).toContain('state=READY');
      req.flush({});
    });

    it('should merge custom filters into query params', () => {
      service.getTasksByDetailedPriorityReport(['PERSONAL'], [1], 'DOMAIN_A', { filter: 'xyz' }).subscribe();

      const req = httpMock.expectOne((r) => r.url.includes('detailed-workbasket-priority-report'));
      expect(req.request.urlWithParams).toContain('filter=xyz');
      req.flush({});
    });
  });
});
