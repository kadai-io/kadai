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
import { DebugElement } from '@angular/core';
import { NgxsModule, Store } from '@ngxs/store';
import { settingsStateMock } from '../../../shared/store/mock-data/mock-store';
import { SettingsState } from '../../../shared/store/settings-store/settings.state';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TaskPriorityReportFilterComponent } from './task-priority-report-filter.component';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialogModule } from '@angular/material/dialog';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('TaskPriorityReportFilterComponent', () => {
  let fixture: ComponentFixture<TaskPriorityReportFilterComponent>;
  let debugElement: DebugElement;
  let component: TaskPriorityReportFilterComponent;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        NgxsModule.forRoot([SettingsState]),
        MatCheckboxModule,
        MatExpansionModule,
        NoopAnimationsModule,
        MatDialogModule,
        TaskPriorityReportFilterComponent
      ],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskPriorityReportFilterComponent);
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

  it('should append filter name to activeFilters list when it is selected', () => {
    component.activeFilters = ['Tasks with state READY'];
    component.emitFilter(true, 'Tasks with state CLAIMED');
    expect(component.activeFilters).toStrictEqual(['Tasks with state READY', 'Tasks with state CLAIMED']);
  });

  it('should remove filter name from list when it is not selected anymore', () => {
    component.activeFilters = ['Tasks with state READY', 'Tasks with state CLAIMED'];
    component.emitFilter(false, 'Tasks with state CLAIMED');
    expect(component.activeFilters).toStrictEqual(['Tasks with state READY']);
  });

  it('should emit query according to values in activeFilters', () => {
    const emitSpy = jest.spyOn(component.applyFilter, 'emit');
    component.activeFilters = ['Tasks with state READY'];
    component.emitFilter(true, 'Tasks with state CLAIMED');
    expect(emitSpy).toBeCalledWith({ state: ['READY', 'CLAIMED'] });
  });
});
