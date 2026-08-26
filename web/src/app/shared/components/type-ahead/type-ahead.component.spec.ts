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

import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { TypeAheadComponent } from './type-ahead.component';
import { AccessIdsService } from '../../services/access-ids/access-ids.service';
import { delay, of, Subject } from 'rxjs';
import { provideStore, Store } from '@ngxs/store';
import { EngineConfigurationState } from '../../store/engine-configuration-store/engine-configuration.state';
import { engineConfigurationMock } from '../../store/mock-data/mock-store';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AccessId } from 'app/shared/models/access-id';

const accessIdService: Partial<AccessIdsService> = {
  searchForAccessId: vi.fn().mockReturnValue(of([{ accessId: 'user-g-1', name: 'Gerda' }]))
};

describe('TypeAheadComponent with AccessId input', () => {
  let fixture: ComponentFixture<TypeAheadComponent>;
  let debugElement: DebugElement;
  let component: TypeAheadComponent;
  let store: Store;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TypeAheadComponent],
      providers: [
        provideStore([EngineConfigurationState]),
        { provide: AccessIdsService, useValue: accessIdService },

        provideHttpClientTesting()
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(Store);
    store.reset({
      engineConfiguration: engineConfigurationMock
    });
    fixture = TestBed.createComponent(TypeAheadComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    httpMock
      .expectOne('environments/data-sources/kadai-customization.json')
      .flush(engineConfigurationMock.customisation);
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch name when typing in an access id', async () => {
    const input = debugElement.nativeElement.querySelector('.type-ahead__input-field');
    expect(input).toBeTruthy();
    input.value = 'user-g-1';
    input.dispatchEvent(new Event('input'));
    component.accessIdForm.get('accessId')!.updateValueAndValidity({ emitEvent: true });

    await vi.waitFor(() => {
      fixture.detectChanges();
      expect(component.name()).toBe('Gerda');
    });
  });

  it('should emit false when an invalid access id is set', async () => {
    const emitSpy = vi.spyOn(component.isFormValid, 'emit');
    fixture.componentRef.setInput('displayError', true);
    component.accessIdForm.get('accessId')!.setValue('invalid-user');
    component.accessIdForm.get('accessId')!.updateValueAndValidity({ emitEvent: true });

    await vi.waitFor(() => {
      fixture.detectChanges();
      expect(emitSpy).toHaveBeenCalledWith(false);
    });
  });

  it('should emit true when a valid access id is set', async () => {
    const emitSpy = vi.spyOn(component.isFormValid, 'emit');
    component.accessIdForm.get('accessId')!.setValue('user-g-1');
    component.accessIdForm.get('accessId')!.updateValueAndValidity({ emitEvent: true });

    await vi.waitFor(() => {
      fixture.detectChanges();
      expect(emitSpy).toHaveBeenCalledWith(true);
    });
  });

  it('should mark the accessId control as touched when invalid and displayError is true', async () => {
    const control = component.accessIdForm.get('accessId');
    const markAsTouchedSpy = vi.spyOn(control!, 'markAsTouched');
    fixture.componentRef.setInput('displayError', true);

    component.accessIdForm.get('accessId')?.setValue('invalid-user');
    component.searchForAccessId('invalid-user');

    await vi.waitFor(() => {
      fixture.detectChanges();
      expect(markAsTouchedSpy).toHaveBeenCalled();
    });
  });

  it('should not emit accessIdEventEmitter when placeHolderMessage is "Search for AccessId"', () => {
    const accessIdEmitSpy = vi.spyOn(component.accessIdEventEmitter, 'emit');
    fixture.componentRef.setInput('placeHolderMessage', 'Search for AccessId');
    component.handleEmptyAccessId();
    expect(accessIdEmitSpy).not.toHaveBeenCalled();
  });

  it('should emit accessIdEventEmitter with emptyAccessId when placeHolderMessage is not "Search for AccessId"', () => {
    const accessIdEmitSpy = vi.spyOn(component.accessIdEventEmitter, 'emit');
    fixture.componentRef.setInput('placeHolderMessage', 'Some other message');
    component.handleEmptyAccessId();
    expect(accessIdEmitSpy).toHaveBeenCalledWith(component.emptyAccessId);
  });

  it('should set errors on accessId control and emit false when isRequired is true in handleEmptyAccessId', () => {
    fixture.componentRef.setInput('isRequired', true);
    const control = component.accessIdForm.get('accessId');
    const setErrorsSpy = vi.spyOn(control!, 'setErrors');
    const emitSpy = vi.spyOn(component.isFormValid, 'emit');
    component.handleEmptyAccessId();
    expect(setErrorsSpy).toHaveBeenCalledWith({ incorrect: true });
    expect(emitSpy).toHaveBeenCalledWith(false);
  });

  it('should not set errors and emit true when isRequired is false in handleEmptyAccessId', () => {
    fixture.componentRef.setInput('isRequired', false);
    const control = component.accessIdForm.get('accessId');
    const setErrorsSpy = vi.spyOn(control!, 'setErrors');
    const emitSpy = vi.spyOn(component.isFormValid, 'emit');
    component.handleEmptyAccessId();
    expect(setErrorsSpy).not.toHaveBeenCalled();
    expect(emitSpy).toHaveBeenCalledWith(true);
  });

  it('should call setAccessIdFromInput when entityId input changes', () => {
    const setAccessIdSpy = vi.spyOn(component, 'setAccessIdFromInput');
    fixture.componentRef.setInput('entityId', 'new-id');
    fixture.detectChanges();
    expect(setAccessIdSpy).toHaveBeenCalled();
  });

  it('should not call setAccessIdFromInput when entityId input does not change', () => {
    fixture.componentRef.setInput('entityId', 'some-id');
    fixture.detectChanges();
    const setAccessIdSpy = vi.spyOn(component, 'setAccessIdFromInput');
    expect(setAccessIdSpy).not.toHaveBeenCalled();
  });

it('should ignore stale/older search requests if a new input was provided (prevent race condition)', async () => {
  const searchSpy = vi.spyOn(accessIdService, 'searchForAccessId')
    .mockReset()
    .mockReturnValueOnce(of([{ accessId: 'user-a', name: 'User A' }]).pipe(delay(50)))   // simulation of a long request
    .mockReturnValueOnce(of([{ accessId: 'user-b', name: 'User B' }]).pipe(delay(10)));  // the second request was sent later but was processed first

  const inputEl: HTMLInputElement = fixture.nativeElement.querySelector('input');

  inputEl.value = 'user-a';
  inputEl.dispatchEvent(new Event('input'));
  fixture.detectChanges();

  await vi.waitFor(() => {
    expect(searchSpy).toHaveBeenCalledTimes(1);
  }, { timeout: 750 });

  inputEl.value = 'user-b';
  inputEl.dispatchEvent(new Event('input'));
  fixture.detectChanges();

  await vi.waitFor(() => {
    expect(searchSpy).toHaveBeenCalledTimes(2);
  }, { timeout: 750 });

  await vi.waitFor(() => {
    fixture.detectChanges();
    expect(component.name()).toBe('User B');
  }, { timeout: 750 });
});
});

describe('TypeAheadComponent without debounceTime configured', () => {
  let fixture: ComponentFixture<TypeAheadComponent>;
  let component: TypeAheadComponent;
  let store: Store;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TypeAheadComponent],
      providers: [
        provideStore([EngineConfigurationState]),
        { provide: AccessIdsService, useValue: accessIdService },

        provideHttpClientTesting()
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(Store);

    const configWithoutDebounce = {
      ...engineConfigurationMock,
      customisation: {
        ...engineConfigurationMock.customisation,
        EN: {
          ...engineConfigurationMock.customisation.EN,
          global: {}
        }
      }
    };
    store.reset({ engineConfiguration: configWithoutDebounce });
    fixture = TestBed.createComponent(TypeAheadComponent);
    component = fixture.componentInstance;
    httpMock.expectOne('environments/data-sources/kadai-customization.json').flush(configWithoutDebounce.customisation);
    fixture.detectChanges();
  });

  it('should keep default debounce time of 750 when debounceTimeLookupField is not configured', () => {
    expect(component.debounceTime).toBe(750);
  });
});
