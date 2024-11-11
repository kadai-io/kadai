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
import { CanvasComponent } from './canvas.component';
import { workbasketReportMock } from '../monitor-mock-data';
import { SettingsState } from '../../../shared/store/settings-store/settings.state';
import { settingsStateMock } from '../../../shared/store/mock-data/mock-store';
import { MatDialogModule } from '@angular/material/dialog';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('CanvasComponent', () => {
  let fixture: ComponentFixture<CanvasComponent>;
  let debugElement: DebugElement;
  let component: CanvasComponent;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([SettingsState]), MatDialogModule],
      providers: [CanvasComponent, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(CanvasComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    component.id = '1';
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

  it('should show canvas with id from input', () => {
    const id = debugElement.nativeElement.querySelector("[id='1']");
    expect(id).toBeTruthy();
  });

  it('should call generateChart()', () => {
    component.generateChart = jest.fn();
    const reportRow = workbasketReportMock.rows[1];
    component.row = reportRow;
    fixture.detectChanges();
    component.ngAfterViewInit();
    expect(component.generateChart).toHaveBeenCalledWith('1', reportRow);
  });
});
