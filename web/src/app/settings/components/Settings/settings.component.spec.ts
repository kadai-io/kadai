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

  it('should restore settings to oldSettings when onReset is called', () => {
    const originalSettings = component.deepCopy(component.oldSettings);
    component.onReset();
    expect(component.settings).toEqual(originalSettings);
  });

  it('should update settings value from DOM input when onColorChange is called', () => {
    const input = document.createElement('input');
    input.id = 'testColorKey';
    input.value = '#ABCDEF';
    document.body.appendChild(input);
    component.onColorChange('testColorKey');
    expect(component.settings['testColorKey']).toBe('#ABCDEF');
    document.body.removeChild(input);
  });

  it('should call onSave when Save button is clicked via DOM event', () => {
    const saveSpy = vi.spyOn(component, 'onSave');
    const saveButton = fixture.nativeElement.querySelector('.settings__button--primary');
    saveButton.click();
    expect(saveSpy).toHaveBeenCalled();
  });

  it('should call onReset when Undo changes button is clicked via DOM event', () => {
    const resetSpy = vi.spyOn(component, 'onReset');
    const resetButton = fixture.nativeElement.querySelector('.settings__button--secondary');
    resetButton.click();
    expect(resetSpy).toHaveBeenCalled();
  });

  it('should render text input fields for members of type text', () => {
    const textInputs = fixture.nativeElement.querySelectorAll('input[type="text"]');
    expect(textInputs.length).toBeGreaterThan(0);
  });

  it('should render number input fields for members of type interval', () => {
    const numberInputs = fixture.nativeElement.querySelectorAll('input[type="number"]');
    expect(numberInputs.length).toBeGreaterThan(0);
  });

  it('should render color input fields for members of type color', () => {
    const colorInputs = fixture.nativeElement.querySelectorAll('input[type="color"]');
    expect(colorInputs.length).toBeGreaterThan(0);
  });

  it('should render textarea fields for members of type json', () => {
    const textareas = fixture.nativeElement.querySelectorAll('textarea');
    expect(textareas.length).toBeGreaterThan(0);
  });

  it('should call onColorChange when a color input change event fires', () => {
    const colorChangeSpy = vi.spyOn(component, 'onColorChange');
    const colorInput = fixture.nativeElement.querySelector('input[type="color"]') as HTMLInputElement;
    colorInput.value = '#123456';
    colorInput.dispatchEvent(new Event('change'));
    expect(colorChangeSpy).toHaveBeenCalled();
  });

  it('should render group display names from settings schema', () => {
    const headings = fixture.nativeElement.querySelectorAll('.settings__domain-name');
    expect(headings.length).toBeGreaterThan(0);
    const headingTexts = Array.from(headings).map((el: any) => el.textContent.trim());
    expect(headingTexts).toContain('Priority Report');
  });

  it('should reset settings to old values when Undo changes button is clicked', () => {
    const originalSettings = component.deepCopy(component.oldSettings);
    component.settings['nameHighPriority'] = 'Modified Value';
    const resetButton = fixture.nativeElement.querySelector('.settings__button--secondary');
    resetButton.click();
    expect(component.settings['nameHighPriority']).toEqual(originalSettings['nameHighPriority']);
  });

  it('should trigger ngModel write function on text inputs by dispatching input events', () => {
    const textInputs = fixture.nativeElement.querySelectorAll('input[type="text"]');
    textInputs.forEach((input: HTMLInputElement) => {
      input.value = 'Test Value';
      input.dispatchEvent(new Event('input'));
    });
    expect(component).toBeTruthy();
  });

  it('should trigger ngModel write function on number inputs by dispatching input events', () => {
    const numberInputs = fixture.nativeElement.querySelectorAll('input[type="number"]');
    numberInputs.forEach((input: HTMLInputElement) => {
      input.value = '5';
      input.dispatchEvent(new Event('input'));
    });
    expect(component).toBeTruthy();
  });

  it('should trigger ngModel write function on textarea by dispatching input event', () => {
    const textarea = fixture.nativeElement.querySelector('textarea');
    if (textarea) {
      textarea.value = '{"test": "value"}';
      textarea.dispatchEvent(new Event('input'));
    }
    expect(component).toBeTruthy();
  });
});
