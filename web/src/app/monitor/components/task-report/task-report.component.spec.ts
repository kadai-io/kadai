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
});
