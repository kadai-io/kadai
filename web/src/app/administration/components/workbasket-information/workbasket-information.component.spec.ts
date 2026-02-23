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
import { WorkbasketInformationComponent } from './workbasket-information.component';
import { DebugElement } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { Observable, of } from 'rxjs';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { ACTION } from '../../../shared/models/action';
import {
  MarkWorkbasketForDeletion,
  RemoveDistributionTarget,
  UpdateWorkbasket
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import {
  engineConfigurationMock,
  selectedWorkbasketMock,
  workbasketReadStateMock
} from '../../../shared/store/mock-data/mock-store';
import { By } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideAngularSvgIcon } from 'angular-svg-icon';

const workbasketServiceMock: Partial<WorkbasketService> = {
  triggerWorkBasketSaved: vi.fn(),
  updateWorkbasket: vi.fn().mockReturnValue(of(true)),
  markWorkbasketForDeletion: vi.fn().mockReturnValue(of(true)),
  createWorkbasket: vi.fn().mockReturnValue(of({ ...selectedWorkbasketMock })),
  getWorkBasket: vi.fn().mockReturnValue(of({ ...selectedWorkbasketMock })),
  getWorkBasketsSummary: vi.fn().mockReturnValue(of({ workbaskets: [] })),
  getWorkBasketAccessItems: vi.fn().mockReturnValue(of({ accessItems: [] })),
  getWorkBasketsDistributionTargets: vi.fn().mockReturnValue(of({ distributionTargets: [] }))
};

const formValidatorServiceMock: Partial<FormsValidatorService> = {
  isFieldValid: vi.fn().mockReturnValue(true),
  validateInputOverflow: vi.fn(),
  validateFormInformation: vi.fn().mockImplementation((): Promise<any> => Promise.resolve(true)),
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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketInformationComponent],
      providers: [
        provideStore([EngineConfigurationState, WorkbasketState]),
        provideRouter([]),
        { provide: WorkbasketService, useValue: workbasketServiceMock },
        { provide: FormsValidatorService, useValue: formValidatorServiceMock },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAngularSvgIcon()
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
  });

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
    const showSuccessSpy = vi.spyOn(notificationService, 'showSuccess');
    component.onUndo();
    expect(showSuccessSpy).toHaveBeenCalled();
    expect(component.workbasket).toMatchObject(component.workbasketClone);
  });

  it('should save workbasket when workbasketId there', async () => {
    component.workbasket = { ...selectedWorkbasketMock };
    component.workbasket.workbasketId = '1';
    component.action = ACTION.COPY;
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(UpdateWorkbasket)).subscribe(() => (actionDispatched = true));
    component.onSave();
    expect(actionDispatched).toBe(true);
    expect(component.workbasketClone).toMatchObject(component.workbasket);
  });

  it('should dispatch MarkWorkbasketforDeletion action when onRemoveConfirmed is called', async () => {
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(MarkWorkbasketForDeletion)).subscribe(() => (actionDispatched = true));
    component.onRemoveConfirmed();
    expect(actionDispatched).toBe(true);
  });

  it('should create new workbasket when workbasketId is undefined', () => {
    component.workbasket.workbasketId = undefined;
    const postNewWorkbasketSpy = vi.spyOn(component, 'postNewWorkbasket');
    component.onSave();
    expect(postNewWorkbasketSpy).toHaveBeenCalled();
  });

  it('should not show custom fields with attribute visible = false', () => {
    const inputCustoms = debugElement.queryAll(By.css('.custom-fields__input'));
    expect(inputCustoms).toHaveLength(3);
  });

  it('should save custom field input at position 4 when custom field at position 3 is not visible', async () => {
    const newValue = 'New value';

    let inputCustom3 = debugElement.nativeElement.querySelector('#wb-custom-3');
    let inputCustom4 = debugElement.nativeElement.querySelector('#wb-custom-4');
    expect(inputCustom3).toBeFalsy();
    expect(inputCustom4).toBeTruthy();
    inputCustom4.value = newValue;
    inputCustom4.dispatchEvent(new Event('input'));

    await fixture.whenStable();

    expect(component.workbasket['custom3']).toBe('');
    expect(component.workbasket['custom4']).toBe(newValue);
  });

  it('should call formsValidatorService.isFieldValid when isFieldValid is called', () => {
    const result = component.isFieldValid('key');
    expect(formValidatorServiceMock.isFieldValid).toHaveBeenCalled();
    expect(result).toBe(true);
  });

  it('should call notificationService.showDialog when removeWorkbasket is called', () => {
    const notificationService = TestBed.inject(NotificationService);
    const showDialogSpy = vi.spyOn(notificationService, 'showDialog').mockImplementation(() => undefined);
    component.removeWorkbasket();
    expect(showDialogSpy).toHaveBeenCalledWith('WORKBASKET_DELETE', expect.any(Object), expect.any(Function));
  });

  it('should dispatch RemoveDistributionTarget when removeDistributionTargets is called', () => {
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(RemoveDistributionTarget)).subscribe(() => (actionDispatched = true));
    component.removeDistributionTargets();
    expect(actionDispatched).toBe(true);
  });

  it('should set workbasket owner when onSelectedOwner is called', () => {
    component.onSelectedOwner({ accessId: 'user-test', name: 'Test User' });
    expect(component.workbasket.owner).toBe('user-test');
  });

  it('should return correct custom property string for getWorkbasketCustomProperty', () => {
    expect(component.getWorkbasketCustomProperty(1)).toBe('custom1');
    expect(component.getWorkbasketCustomProperty(4)).toBe('custom4');
    expect(component.getWorkbasketCustomProperty(12)).toBe('custom12');
  });

  it('should handle workbasket customization without owner config', () => {
    const engineConfigWithoutOwner = {
      ...engineConfigurationMock,
      customisation: {
        ...engineConfigurationMock.customisation,
        EN: {
          ...engineConfigurationMock.customisation.EN,
          workbaskets: {
            ...engineConfigurationMock.customisation.EN.workbaskets,
            information: {
              custom1: engineConfigurationMock.customisation.EN.workbaskets.information.custom1,
              custom3: engineConfigurationMock.customisation.EN.workbaskets.information.custom3
            }
          }
        }
      }
    };
    store.reset({ ...store.snapshot(), engineConfiguration: engineConfigWithoutOwner });
    component.ngOnInit();
    expect(component).toBeTruthy();
  });

  it('should not validate when validateInputOverflow is called with undefined value', () => {
    const mockModel: any = { name: 'testField', value: undefined };
    component.validateInputOverflow(mockModel, 10);
    expect(component).toBeTruthy();
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const nextSpy = vi.spyOn(component.destroy$, 'next');
    const completeSpy = vi.spyOn(component.destroy$, 'complete');
    component.ngOnDestroy();
    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should set requestInProgress to true when beforeRequest is called', () => {
    const requestInProgressService = TestBed.inject(RequestInProgressService);
    const setRequestSpy = vi.spyOn(requestInProgressService, 'setRequestInProgress');
    component.beforeRequest();
    expect(setRequestSpy).toHaveBeenCalledWith(true);
  });

  it('should set requestInProgress to false and trigger saved when afterRequest is called', () => {
    const requestInProgressService = TestBed.inject(RequestInProgressService);
    const setRequestSpy = vi.spyOn(requestInProgressService, 'setRequestInProgress');
    component.afterRequest();
    expect(setRequestSpy).toHaveBeenCalledWith(false);
    expect(workbasketServiceMock.triggerWorkBasketSaved).toHaveBeenCalled();
  });

  it('should set created and modified dates when addDateToWorkbasket is called', () => {
    component.addDateToWorkbasket();
    expect(component.workbasket.created).toBeDefined();
    expect(component.workbasket.modified).toBeDefined();
  });

  it('should call postNewWorkbasket when workbasketId is falsy in onSave', () => {
    component.workbasket = { ...selectedWorkbasketMock };
    component.workbasket.workbasketId = undefined;
    const postSpy = vi.spyOn(component, 'postNewWorkbasket').mockImplementation(() => {});
    component.onSave();
    expect(postSpy).toHaveBeenCalled();
  });
});
