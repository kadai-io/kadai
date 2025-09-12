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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { ClassificationState } from '../../../shared/store/classification-store/classification.state';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { ClassificationListComponent } from './classification-list.component';
import { classificationStateMock, engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';
import { Observable, of } from 'rxjs';
import {
  CreateClassification,
  GetClassifications
} from '../../../shared/store/classification-store/classification.actions';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { By } from '@angular/platform-browser';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { provideHttpClient } from '@angular/common/http';
import { Location } from '@angular/common';
import { ImportExportService } from '../../services/import-export.service';

jest.mock('angular-svg-icon');

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomainValue: jest.fn().mockReturnValue(of()),
  getSelectedDomain: jest.fn().mockReturnValue(of()),
  getDomains: jest.fn().mockReturnValue(of())
};

const requestInProgressServiceSpy: Partial<RequestInProgressService> = {
  setRequestInProgress: jest.fn().mockReturnValue(of()),
  getRequestInProgress: jest.fn().mockReturnValue(of(false))
};

const locationSpy: Partial<Location> = {
  path: jest.fn().mockReturnValue('/administration/classifications'),
  go: jest.fn()
};

describe('ClassificationListComponent', () => {
  let fixture: ComponentFixture<ClassificationListComponent>;
  let debugElement: DebugElement;
  let component: ClassificationListComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ClassificationListComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        provideStore([ClassificationState, EngineConfigurationState]),
        provideHttpClient(),
        { provide: DomainService, useValue: domainServiceSpy },
        { provide: RequestInProgressService, useValue: requestInProgressServiceSpy },
        { provide: Location, useValue: locationSpy }
      ]
    }).compileComponents();

    TestBed.overrideComponent(ClassificationListComponent, { set: { schemas: [NO_ERRORS_SCHEMA] } });
    fixture = TestBed.createComponent(ClassificationListComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    actions$ = TestBed.inject(Actions);
    store.reset({
      ...store.snapshot(),
      classification: classificationStateMock,
      engineConfiguration: engineConfigurationMock
    });
    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  /* HTML: ACTION TOOLBAR */
  it('should call CreateClassification when add-classification button is clicked', async () => {
    const button = debugElement.nativeElement.querySelector('.classification-list__add-button');
    expect(button).toBeTruthy();
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(CreateClassification)).subscribe(() => (actionDispatched = true));
    button.click();
    expect(actionDispatched).toBe(true);
  });

  it('should display import-export component', () => {
    expect(debugElement.nativeElement.querySelector('kadai-administration-import-export')).toBeTruthy();
  });

  it('should display classification-types-selector component', () => {
    const typesSelectorComponent = debugElement.nativeElement.querySelector(
      'kadai-administration-classification-types-selector'
    );
    expect(typesSelectorComponent).toBeTruthy();
  });

  /* HTML: FILTER */
  it('should display filter input field', () => {
    const button = debugElement.nativeElement.querySelector('.classification-list__input-field');
    expect(button).toBeTruthy();
    expect(button.textContent).toBe('Filter classification');
  });

  it('should display filter button', () => {
    const button = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    expect(button).toBeTruthy();
    expect(button.textContent.trim()).toBe('filter_list');
  });

  it('should change selectedCategory property when button is clicked', () => {
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    filterButton.click();
    fixture.detectChanges();
    component.selectedCategory = 'EXTERNAL';
    const allButton = debugElement.query(By.css('.classification-list__all-button'));
    expect(allButton).toBeTruthy();
    allButton.nativeElement.click();
    expect(component.selectedCategory).toBe('');
  });

  it('should display list of categories which can be selected', () => {
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    filterButton.click();
    fixture.detectChanges();
    const matMenu = debugElement.queryAll(By.css('.classification-list__categories'));
    expect(matMenu.length).toBe(3);
  });

  /* HTML: CLASSIFICATION TREE */
  it('should display tree component when classifications exist', () => {
    component.classifications = [{ classificationId: '1' }, { classificationId: '2' }];
    fixture.detectChanges();
    expect(debugElement.nativeElement.querySelector('kadai-administration-tree')).toBeTruthy();
  });

  it('should display icon and text when no classifications exist', () => {
    const noClassifications = debugElement.nativeElement.querySelector('.classification-list__no-items');
    expect(noClassifications.childNodes.length).toBe(1);
    expect(noClassifications.childNodes[0].textContent).toBe('There are no classifications');
  });

  /* TS: getCategoryIcon() */
  it('should return icon for category when getCategoryIcon is called and category exists', (done) => {
    const categoryIcon = component.getCategoryIcon('MANUAL');
    categoryIcon.subscribe((iconPair) => {
      expect(iconPair.left).toBe('assets/icons/categories/manual.svg');
      expect(iconPair.right).toBe('MANUAL');
      done();
    });
  });

  it('should return a special icon when getCategoryIcon is called and category does not exist', (done) => {
    const categoryIcon = component.getCategoryIcon('CLOUD');
    categoryIcon.subscribe((iconPair) => {
      expect(iconPair.left).toBe('assets/icons/categories/missing-icon.svg');
      done();
    });
  });

  it('should return the "all" icon and label when getCategoryIcon is called with empty category', (done) => {
    const customEngineConfig = JSON.parse(JSON.stringify(engineConfigurationMock));
    customEngineConfig.customisation.EN.classifications.categories.all = 'assets/icons/categories/all.svg';
    store.reset({
      ...store.snapshot(),
      engineConfiguration: customEngineConfig
    });
    const categoryIcon$ = component.getCategoryIcon('');
    categoryIcon$.subscribe((iconPair) => {
      expect(iconPair.left).toBe('assets/icons/categories/all.svg');
      expect(iconPair.right).toBe('All');
      done();
    });
  });

  it('should navigate to new-classification on addClassification and dispatch CreateClassification', () => {
    (locationSpy.path as jest.Mock).mockReturnValue('/administration/classifications');
    let dispatched = false;
    actions$.pipe(ofActionDispatched(CreateClassification)).subscribe(() => (dispatched = true));
    component.addClassification();
    expect(dispatched).toBe(true);
    expect(locationSpy.go).toHaveBeenCalledWith('/administration/classifications/new-classification');
  });

  it('should toggle requestInProgress when GetClassifications is dispatched and completed', async () => {
    (requestInProgressServiceSpy.setRequestInProgress as jest.Mock).mockClear();
    await store.dispatch(new (class extends (CreateClassification as any).constructor {})()); // noop to flush queue
    await store.dispatch(new (GetClassifications as any)());
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(true);
    // ofActionCompleted should fire and set false as well
    expect((requestInProgressServiceSpy.setRequestInProgress as jest.Mock).mock.calls.some((c) => c[0] === false)).toBe(
      true
    );
  });

  it('should reset selectedCategory and dispatch GetClassifications when classification type changes', (done) => {
    component.selectedCategory = 'MANUAL';
    let dispatched = false;
    const sub = actions$.pipe(ofActionDispatched(GetClassifications)).subscribe(() => (dispatched = true));
    const newState = {
      ...store.snapshot(),
      classification: { ...classificationStateMock, selectedClassificationType: 'TASK' }
    } as any;
    store.reset(newState);
    setTimeout(() => {
      expect(component.selectedCategory).toBe('');
      expect(dispatched).toBe(true);
      sub.unsubscribe();
      done();
    }, 0);
  });

  it('should delegate setRequestInProgress to the service', () => {
    (requestInProgressServiceSpy.setRequestInProgress as jest.Mock).mockClear();
    component.setRequestInProgress(true);
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(true);
  });

  it('should set selected category when selectCategory is called', () => {
    component.selectCategory('EXTERNAL');
    expect(component.selectedCategory).toBe('EXTERNAL');
  });

  it('should update requestInProgress from the service on init', () => {
    // service returns of(false), so component.requestInProgress should be false after init
    expect(component.requestInProgress).toBe(false);
  });

  it('should unsubscribe from actions on destroy (no more setRequestInProgress calls)', () => {
    (requestInProgressServiceSpy.setRequestInProgress as jest.Mock).mockClear();
    component.ngOnDestroy();
    store.dispatch(new (GetClassifications as any)());
    expect(requestInProgressServiceSpy.setRequestInProgress).not.toHaveBeenCalled();
  });

  it('should dispatch GetClassifications when import/export finishes', (done) => {
    const importExportService = TestBed.inject(ImportExportService);
    let dispatched = false;
    const sub = actions$.pipe(ofActionDispatched(GetClassifications)).subscribe(() => (dispatched = true));
    importExportService.setImportingFinished(true);
    setTimeout(() => {
      expect(dispatched).toBe(true);
      sub.unsubscribe();
      done();
    }, 0);
  });

  it('should dispatch on domain change subscription in ngOnInit', (done) => {
    const dispatchSpy = jest.spyOn(store, 'dispatch');
    (domainServiceSpy.getSelectedDomain as jest.Mock).mockReturnValue(of('X'));
    component.ngOnDestroy();
    component.ngOnInit();
    setTimeout(() => {
      expect(dispatchSpy).toHaveBeenCalledWith(GetClassifications);
      done();
    }, 0);
  });
});
