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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Component, DebugElement, Input, Pipe, PipeTransform } from '@angular/core';
import { NgxsModule, Store } from '@ngxs/store';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { TaskPriorityReportComponent } from './task-priority-report.component';
import { MonitorService } from '../../services/monitor.service';
import { of } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { workbasketReportMock } from '../monitor-mock-data';
import { settingsStateMock } from '../../../shared/store/mock-data/mock-store';
import { SettingsState } from '../../../shared/store/settings-store/settings.state';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatDividerModule } from '@angular/material/divider';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';

@Component({ selector: 'kadai-monitor-canvas', template: '' })
class CanvasStub {
  @Input() row;
  @Input() id;
  @Input() isReversed;
}

@Component({ selector: 'kadai-monitor-task-priority-report-filter', template: '' })
class TaskPriorityReportFilterStub {}

const monitorServiceSpy: Partial<MonitorService> = {
  getTasksByPriorityReport: jest.fn().mockReturnValue(of(workbasketReportMock))
};

const notificationServiceSpy: Partial<NotificationService> = {
  showWarning: jest.fn()
};

describe('TaskPriorityReportComponent', () => {
  let fixture: ComponentFixture<TaskPriorityReportComponent>;
  let debugElement: DebugElement;
  let component: TaskPriorityReportComponent;

  beforeEach(waitForAsync(() => {
    registerLocaleData(localeDe);

    TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([SettingsState]), MatTableModule, MatDividerModule, NoopAnimationsModule],
      declarations: [CanvasStub, TaskPriorityReportFilterStub],
      providers: [
        RequestInProgressService,
        { provide: MonitorService, useValue: monitorServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        TaskPriorityReportComponent
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskPriorityReportComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    const store: Store = TestBed.inject(Store);
    store.reset({
      ...store.snapshot(),
      settings: settingsStateMock
    });
    fixture.detectChanges();
  }));

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should show Canvas component for all Workbaskets', () => {
    const canvas = debugElement.nativeElement.querySelectorAll('kadai-monitor-canvas');
    expect(canvas).toHaveLength(2);
  });

  it('should show table for all Workbaskets', () => {
    const table = debugElement.nativeElement.querySelectorAll('table');
    expect(table).toHaveLength(2);
  });

  it('should not show warning when actual header matches the expected header', () => {
    const showWarningSpy = jest.spyOn(notificationServiceSpy, 'showWarning');
    component.ngOnInit();
    expect(showWarningSpy).toHaveBeenCalledTimes(0);
  });
});
