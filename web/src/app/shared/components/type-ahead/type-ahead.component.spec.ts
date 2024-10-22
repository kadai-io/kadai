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

import { ComponentFixture, fakeAsync, flush, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { TypeAheadComponent } from './type-ahead.component';
import { AccessIdsService } from '../../services/access-ids/access-ids.service';
import { of } from 'rxjs';
import { NgxsModule, Store } from '@ngxs/store';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EngineConfigurationState } from '../../store/engine-configuration-store/engine-configuration.state';
import { ClassificationCategoriesService } from '../../services/classification-categories/classification-categories.service';
import { engineConfigurationMock } from '../../store/mock-data/mock-store';
import { provideHttpClient } from '@angular/common/http';

const accessIdService: Partial<AccessIdsService> = {
  searchForAccessId: jest.fn().mockReturnValue(of([{ accessId: 'user-g-1', name: 'Gerda' }]))
};

describe('TypeAheadComponent with AccessId input', () => {
  let fixture: ComponentFixture<TypeAheadComponent>;
  let debugElement: DebugElement;
  let component: TypeAheadComponent;
  let store: Store;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        NgxsModule.forRoot([EngineConfigurationState]),
        MatFormFieldModule,
        MatInputModule,
        MatAutocompleteModule,
        MatTooltipModule,
        NoopAnimationsModule,
        FormsModule,
        ReactiveFormsModule
      ],
      declarations: [TypeAheadComponent],
      providers: [
        { provide: AccessIdsService, useValue: accessIdService },
        ClassificationCategoriesService,
        provideHttpClient()
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
    store.reset({
      engineConfiguration: engineConfigurationMock
    });
    fixture = TestBed.createComponent(TypeAheadComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch name when typing in an access id', fakeAsync(() => {
    const input = debugElement.nativeElement.querySelector('.type-ahead__input-field');
    expect(input).toBeTruthy();
    input.value = 'user-g-1';
    input.dispatchEvent(new Event('input'));
    component.accessIdForm.get('accessId').updateValueAndValidity({ emitEvent: true });

    tick(50);
    expect(component.name).toBe('Gerda');
  }));

  it('should emit false when an invalid access id is set', fakeAsync(() => {
    const emitSpy = jest.spyOn(component.isFormValid, 'emit');
    component.displayError = true;
    component.accessIdForm.get('accessId').setValue('invalid-user');
    component.accessIdForm.get('accessId').updateValueAndValidity({ emitEvent: true });

    tick(50);
    fixture.detectChanges();
    flush();

    expect(emitSpy).toHaveBeenCalledWith(false);
  }));

  it('should emit true when a valid access id is set', fakeAsync(() => {
    const emitSpy = jest.spyOn(component.isFormValid, 'emit');
    component.accessIdForm.get('accessId').setValue('user-g-1');
    component.accessIdForm.get('accessId').updateValueAndValidity({ emitEvent: true });

    tick(50);
    fixture.detectChanges();
    flush();

    expect(emitSpy).toHaveBeenCalledWith(true);
  }));
});
