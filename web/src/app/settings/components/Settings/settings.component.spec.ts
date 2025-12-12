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

import { DebugElement } from '@angular/core';
import { Observable } from 'rxjs';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { SettingsState } from '../../../shared/store/settings-store/settings.state';
import { SettingsComponent } from './settings.component';
import { settingsStateMock } from '../../../shared/store/mock-data/mock-store';
import { SetSettings } from '../../../shared/store/settings-store/settings.actions';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const notificationServiceSpy: Partial<NotificationService> = {
  showError: vi.fn(),
  showSuccess: vi.fn(),
  showDialog: vi.fn()
};

describe('SettingsComponent', () => {
  let fixture: ComponentFixture<SettingsComponent>;
  let debugElement: DebugElement;
  let component: SettingsComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SettingsComponent],
      providers: [
        provideStore([SettingsState]),
        {
          provide: NotificationService,
          useValue: notificationServiceSpy
        },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      settings: settingsStateMock
    });
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should show success when form is saved successfully', () => {
    const showSuccessSpy = vi.spyOn(notificationServiceSpy, 'showSuccess');
    component.onSave();
    expect(showSuccessSpy).toHaveBeenCalled();
  });

  it('should show error when an invalid form is tried to be saved', () => {
    component.settings['intervalHighPriority'] = [-100, 100];
    const showErrorSpy = vi.spyOn(notificationServiceSpy, 'showError');
    component.onSave();
    expect(showErrorSpy).toHaveBeenCalled();
  });

  it('should dispatch action onValidate() returns true', async () => {
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SetSettings)).subscribe(() => (isActionDispatched = true));
    component.onSave();
    expect(isActionDispatched).toBe(true);
  });
});
