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
import { DebugElement } from '@angular/core';
import { TypeAheadComponent } from './type-ahead.component';
import { AccessIdsService } from '../../services/access-ids/access-ids.service';
import { of } from 'rxjs';
import { provideStore, Store } from '@ngxs/store';
import { EngineConfigurationState } from '../../store/engine-configuration-store/engine-configuration.state';
import { engineConfigurationMock } from '../../store/mock-data/mock-store';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';

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
        provideHttpClient(),
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
    component.accessIdForm.get('accessId').updateValueAndValidity({ emitEvent: true });

    fixture.detectChanges();
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(component.name).toBe('Gerda');
  });

  it('should emit false when an invalid access id is set', async () => {
    const emitSpy = vi.spyOn(component.isFormValid, 'emit');
    component.displayError = true;
    component.accessIdForm.get('accessId').setValue('invalid-user');
    component.accessIdForm.get('accessId').updateValueAndValidity({ emitEvent: true });

    fixture.detectChanges();
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(emitSpy).toHaveBeenCalledWith(false);
  });

  it('should emit true when a valid access id is set', async () => {
    const emitSpy = vi.spyOn(component.isFormValid, 'emit');
    component.accessIdForm.get('accessId').setValue('user-g-1');
    component.accessIdForm.get('accessId').updateValueAndValidity({ emitEvent: true });

    fixture.detectChanges();
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(emitSpy).toHaveBeenCalledWith(true);
  });

  it('should mark the accessId control as touched when invalid and displayError is true', async () => {
    const control = component.accessIdForm.get('accessId');
    const markAsTouchedSpy = vi.spyOn(control!, 'markAsTouched');
    component.displayError = true;

    component.accessIdForm.get('accessId')?.setValue('invalid-user');
    component.searchForAccessId('invalid-user');

    fixture.detectChanges();
    await new Promise((resolve) => setTimeout(resolve, 100));

    expect(markAsTouchedSpy).toHaveBeenCalled();
  });
});
