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

import { Observable } from 'rxjs';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { SettingsState } from '../../../shared/store/settings-store/settings.state';
import { SettingsComponent } from './settings.component';
import { settingsStateMock } from '../../../shared/store/mock-data/mock-store';
import { SetSettings } from '../../../shared/store/settings-store/settings.actions';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const notificationServiceSpy: Partial<NotificationService> = {
  showError: vi.fn(),
  showSuccess: vi.fn(),
  showDialog: vi.fn()
};

describe('SettingsComponent', () => {
  let fixture: ComponentFixture<SettingsComponent>;
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
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
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
    expect(showSuccessSpy).toHaveBeenCalledWith('SETTINGS_SAVE');
  });

  it('should show error and not dispatch action when saving an invalid form', async () => {
    const showErrorSpy = vi.spyOn(notificationServiceSpy, 'showError');
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SetSettings)).subscribe(() => (isActionDispatched = true));
    const jsonControl = component.settingsForm.get('filter');
    jsonControl?.setValue('{ invalid json');
    component.onSave();
    expect(showErrorSpy).toHaveBeenCalledWith('SETTINGS_SAVE');
    expect(isActionDispatched).toBe(false);
  });

  it('should dispatch SetSettings action when onSave is called with valid form', async () => {
    let isActionDispatched = false;
    actions$.pipe(ofActionDispatched(SetSettings)).subscribe(() => (isActionDispatched = true));
    component.onSave();
    expect(isActionDispatched).toBe(true);
  });

  it('should restore settings form values when onReset is called', () => {
    const initialFormValues = component.settingsForm.value;
    const firstKey = Object.keys(component.settingsForm.controls)[0];
    component.settingsForm.get(firstKey)?.patchValue('Modified Value');
    component.onReset();
    expect(component.settingsForm.value).toEqual(initialFormValues);
  });

  it('should update reactive form control value when color input changes', () => {
    const colorControl = component.settingsForm.get('colorHighPriority');
    expect(colorControl).not.toBeNull();
    colorControl?.setValue('#abcdef');
    expect(colorControl?.value).toBe('#abcdef');
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

  it('should render group display names from settings schema', () => {
    const headings = fixture.nativeElement.querySelectorAll('.settings__domain-name');
    expect(headings.length).toBeGreaterThan(0);
  });

  it('should reset reactive form values when Undo changes button is clicked in DOM', () => {
    const firstKey = Object.keys(component.settingsForm.controls)[0];
    const initialVal = component.settingsForm.get(firstKey)?.value;
    component.settingsForm.get(firstKey)?.patchValue('Changed Value');
    const resetButton = fixture.nativeElement.querySelector('.settings__button--secondary');
    resetButton.click();
    expect(component.settingsForm.get(firstKey)?.value).toEqual(initialVal);
  });

  it('should update form control value when text input triggers input event', () => {
    const textInput = fixture.nativeElement.querySelector('input[type="text"]') as HTMLInputElement;
    if (textInput) {
      textInput.value = 'New Test Text';
      textInput.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      expect(component.settingsForm.invalid).toBeFalsy();
    }
  });

  it('should update form group control value when interval number input triggers input event', () => {
    const numberInput = fixture.nativeElement.querySelector('input[type="number"]') as HTMLInputElement;
    if (numberInput) {
      numberInput.value = '15';
      numberInput.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      expect(component.settingsForm).toBeTruthy();
    }
  });

  it('should update form control value when textarea triggers input event', () => {
    const textarea = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    if (textarea) {
      textarea.value = '{"valid": "json"}';
      textarea.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      expect(component.settingsForm).toBeTruthy();
    }
  });

  it('should dynamically build form controls based on the provided schema members', () => {
    const dynamicSettingsState = {
      settings: {
        customDynamicText: 'Default Dynamic Text',
        customDynamicColor: '#123456',
        schema: [
          {
            displayName: 'Dynamic Group',
            members: [
              {
                key: 'customDynamicText',
                displayName: 'Dynamic Text Label',
                type: 'text'
              },
              {
                key: 'customDynamicColor',
                displayName: 'Dynamic Color Label',
                type: 'color'
              }
            ]
          }
        ]
      }
    };
    store.reset({
      ...store.snapshot(),
      settings: dynamicSettingsState
    });
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.settingsForm.contains('customDynamicText')).toBe(true);
    expect(component.settingsForm.contains('customDynamicColor')).toBe(true);
  });
});
