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
import { WorkbasketAccessItemsComponent } from './workbasket-access-items.component';
import { Component, DebugElement, Input } from '@angular/core';
import { Actions, NgxsModule, ofActionDispatched, Store } from '@ngxs/store';
import { Observable, of } from 'rxjs';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TypeAheadComponent } from '../../../shared/components/type-ahead/type-ahead.component';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { WorkbasketState } from '../../../shared/store/workbasket-store/workbasket.state';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { ClassificationCategoriesService } from '../../../shared/services/classification-categories/classification-categories.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { WorkbasketService } from '../../../shared/services/workbasket/workbasket.service';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { RouterTestingModule } from '@angular/router/testing';
import { SelectedRouteService } from '../../../shared/services/selected-route/selected-route';
import { StartupService } from '../../../shared/services/startup/startup.service';
import { KadaiEngineService } from '../../../shared/services/kadai-engine/kadai-engine.service';
import { WindowRefService } from '../../../shared/services/window/window.service';
import {
  engineConfigurationMock,
  selectedWorkbasketMock,
  workbasketAccessItemsMock
} from '../../../shared/store/mock-data/mock-store';
import {
  GetWorkbasketAccessItems,
  UpdateWorkbasketAccessItems
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

@Component({ selector: 'kadai-shared-spinner', template: '' })
class SpinnerStub {
  @Input() isRunning: boolean;
  @Input() positionClass: string;
}

const requestInProgressServiceSpy: Partial<RequestInProgressService> = {
  setRequestInProgress: jest.fn()
};

const showDialogFn = jest.fn().mockReturnValue(true);
const notificationServiceSpy: Partial<NotificationService> = {
  showSuccess: showDialogFn
};

const validateFormInformationFn = jest.fn().mockImplementation((): Promise<any> => Promise.resolve(true));
const formValidatorServiceSpy: Partial<FormsValidatorService> = {
  isFieldValid: jest.fn().mockReturnValue(true),
  validateInputOverflow: jest.fn(),
  validateFormInformation: validateFormInformationFn,
  get inputOverflowObservable(): Observable<Map<string, boolean>> {
    return of(new Map<string, boolean>());
  }
};

describe('WorkbasketAccessItemsComponent', () => {
  let fixture: ComponentFixture<WorkbasketAccessItemsComponent>;
  let debugElement: DebugElement;
  let component: WorkbasketAccessItemsComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        TypeaheadModule.forRoot(),
        NgxsModule.forRoot([WorkbasketState, EngineConfigurationState]),
        RouterTestingModule.withRoutes([]),
        NoopAnimationsModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatAutocompleteModule,
        MatProgressBarModule,
        MatCheckboxModule,
        MatIconModule,
        MatTooltipModule
      ],
      declarations: [WorkbasketAccessItemsComponent, TypeAheadComponent],
      providers: [
        SpinnerStub,
        {
          provide: RequestInProgressService,
          useValue: requestInProgressServiceSpy
        },
        {
          provide: FormsValidatorService,
          useValue: formValidatorServiceSpy
        },
        {
          provide: NotificationService,
          useValue: notificationServiceSpy
        },
        ClassificationCategoriesService,
        WorkbasketService,
        DomainService,
        SelectedRouteService,
        StartupService,
        KadaiEngineService,
        WindowRefService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(WorkbasketAccessItemsComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    component.workbasket = { ...selectedWorkbasketMock };
    component.accessItemsRepresentation = workbasketAccessItemsMock;
    store.reset({
      ...store.snapshot(),
      engineConfiguration: engineConfigurationMock,
      workbasket: {
        workbasketAccessItems: workbasketAccessItemsMock
      }
    });
  }));

  afterEach(waitForAsync(() => {
    component.workbasket = { ...selectedWorkbasketMock };
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize when accessItems exist', waitForAsync(() => {
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(GetWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.init();
    expect(actionDispatched).toBe(true);
  }));

  it("should discard initializing when accessItems don't exist", () => {
    component.workbasket._links.accessItems = null;
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(GetWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.init();
    expect(actionDispatched).toBe(false);
  });

  it('should call access items sorting when access items are obtained from store', () => {
    const sortSpy = jest.spyOn(component, 'sortAccessItems');
    component.ngOnInit();
    expect(sortSpy).toHaveBeenCalled();
  });

  it('should sort access items by given value', () => {
    const accessItems = component.accessItemsRepresentation.accessItems;
    expect(accessItems[0].accessId).toBe('user-b-1');
    expect(accessItems[1].accessId).toBe('user-b-0');
    component.sortAccessItems(accessItems, 'accessId');
    expect(accessItems[0].accessId).toBe('user-b-0');
    expect(accessItems[1].accessId).toBe('user-b-1');
  });

  it('should add accessItems when add access item button is clicked', () => {
    fixture.detectChanges();
    const addAccessItemButton = debugElement.nativeElement.querySelector(
      'button.workbasket-access-items__buttons-add-access'
    );
    const clearSpy = jest.spyOn(component, 'addAccessItem');

    addAccessItemButton.click();
    expect(clearSpy).toHaveBeenCalled();
  });

  it('should undo changes when undo button is clicked', () => {
    fixture.detectChanges();
    const clearSpy = jest.spyOn(component, 'clear');
    component.clear();
    expect(clearSpy).toHaveBeenCalled();
  });

  it('should check all permissions when check all box is checked', () => {
    fixture.detectChanges();
    const checkAllSpy = jest.spyOn(component, 'checkAll');
    const checkAllButton = debugElement.nativeElement.querySelector('#checkbox-0-00');
    expect(checkAllButton).toBeTruthy();
    checkAllButton.click();
    expect(checkAllSpy).toHaveBeenCalled();
  });

  it('should dispatch UpdateWorkbasketAccessItems action when save button is triggered', () => {
    component.accessItemsRepresentation._links.self.href = 'https://link.mock';
    const onSaveSpy = jest.spyOn(component, 'onSave');
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(UpdateWorkbasketAccessItems)).subscribe(() => (actionDispatched = true));
    component.onSave();
    expect(onSaveSpy).toHaveBeenCalled();
    expect(actionDispatched).toBe(true);
  });
});
