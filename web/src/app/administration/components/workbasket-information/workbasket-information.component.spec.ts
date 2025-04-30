/*
 * Copyright [2025] [envite consulting GmbH]
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
import { WorkbasketInformationComponent } from './workbasket-information.component';
import { DebugElement } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { EMPTY, Observable, of } from 'rxjs';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { ACTION } from '../../../shared/models/action';
import { MarkWorkbasketForDeletion, UpdateWorkbasket } from '../../../shared/store/workbasket-store/workbasket.actions';
import {
  engineConfigurationMock,
  selectedWorkbasketMock,
  workbasketReadStateMock
} from '../../../shared/store/mock-data/mock-store';
import { By } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

jest.mock('angular-svg-icon');

const workbasketServiceMock: Partial<WorkbasketService> = {
  triggerWorkBasketSaved: jest.fn(),
  updateWorkbasket: jest.fn().mockReturnValue(of(true)),
  markWorkbasketForDeletion: jest.fn().mockReturnValue(of(true)),
  createWorkbasket: jest.fn().mockReturnValue(of({ ...selectedWorkbasketMock })),
  getWorkBasket: jest.fn().mockReturnValue(of({ ...selectedWorkbasketMock })),
  getWorkBasketAccessItems: jest.fn().mockReturnValue(EMPTY),
  getWorkBasketsDistributionTargets: jest.fn().mockReturnValue(EMPTY)
};

const formValidatorServiceMock: Partial<FormsValidatorService> = {
  isFieldValid: jest.fn().mockReturnValue(true),
  validateInputOverflow: jest.fn(),
  validateFormInformation: jest.fn().mockImplementation((): Promise<any> => Promise.resolve(true)),
  get inputOverflowObservable(): Observable<Map<string, boolean>> {
    return of(new Map<string, boolean>());
  }
};

describe('WorkbasketInformationComponent', () => {
  let fixture: ComponentFixture<WorkbasketInformationComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketInformationComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WorkbasketInformationComponent],
      providers: [
        provideStore([EngineConfigurationState, WorkbasketState]),
        provideRouter([]),
        { provide: WorkbasketService, useValue: workbasketServiceMock },
        { provide: FormsValidatorService, useValue: formValidatorServiceMock },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketInformationComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      engineConfiguration: engineConfigurationMock,
      workbasket: workbasketReadStateMock
    });
    component.workbasket = selectedWorkbasketMock;

    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should display custom fields correctly', () => {
    const customFields = debugElement.nativeElement.getElementsByClassName('custom-fields__form-field');
    expect(customFields.length).toBe(3); //mock data has custom1->4 but engineConfig disables custom3 -> [1,2,4]
  });

  it('should create clone of workbasket when workbasket value changes', () => {
    component.action = ACTION.READ;
    component.ngOnChanges();
    expect(component.workbasketClone).toMatchObject(component.workbasket);
  });

  it('should submit when validatorService is true', () => {
    const formsValidatorService = TestBed.inject(FormsValidatorService);
    component.onSubmit();
    expect(formsValidatorService.formSubmitAttempt).toBe(true);
  });

  it('should reset workbasket information when onUndo is called', () => {
    component.workbasketClone = selectedWorkbasketMock;
    const notificationService = TestBed.inject(NotificationService);
    const showSuccessSpy = jest.spyOn(notificationService, 'showSuccess');
    component.onUndo();
    expect(showSuccessSpy).toHaveBeenCalled();
    expect(component.workbasket).toMatchObject(component.workbasketClone);
  });

  it('should save workbasket when workbasketId there', waitForAsync(() => {
    component.workbasket = { ...selectedWorkbasketMock };
    component.workbasket.workbasketId = '1';
    component.action = ACTION.COPY;
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(UpdateWorkbasket)).subscribe(() => (actionDispatched = true));
    component.onSave();
    expect(actionDispatched).toBe(true);
    expect(component.workbasketClone).toMatchObject(component.workbasket);
  }));

  it('should dispatch MarkWorkbasketforDeletion action when onRemoveConfirmed is called', waitForAsync(() => {
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(MarkWorkbasketForDeletion)).subscribe(() => (actionDispatched = true));
    component.onRemoveConfirmed();
    expect(actionDispatched).toBe(true);
  }));

  it('should create new workbasket when workbasketId is undefined', () => {
    component.workbasket.workbasketId = undefined;
    const postNewWorkbasketSpy = jest.spyOn(component, 'postNewWorkbasket');
    component.onSave();
    expect(postNewWorkbasketSpy).toHaveBeenCalled();
  });

  it('should not show custom fields with attribute visible = false', () => {
    const inputCustoms = debugElement.queryAll(By.css('.custom-fields__input'));
    expect(inputCustoms).toHaveLength(3);
  });

  it('should save custom field input at position 4 when custom field at position 3 is not visible', fakeAsync(() => {
    const newValue = 'New value';

    let inputCustom3 = debugElement.nativeElement.querySelector('#wb-custom-3');
    let inputCustom4 = debugElement.nativeElement.querySelector('#wb-custom-4');
    expect(inputCustom3).toBeFalsy();
    expect(inputCustom4).toBeTruthy();
    inputCustom4.value = newValue;
    inputCustom4.dispatchEvent(new Event('input'));

    tick();
    fixture.detectChanges();
    flush();

    expect(component.workbasket['custom3']).toBe('');
    expect(component.workbasket['custom4']).toBe(newValue);
  }));
});
