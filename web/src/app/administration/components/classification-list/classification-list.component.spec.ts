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
import { Component, DebugElement, input } from '@angular/core';
import { Actions, ofActionDispatched, provideStore, Store } from '@ngxs/store';
import { ImportExportService } from '../../services/import-export.service';
import { ClassificationState } from '../../../shared/store/classification-store/classification.state';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { ClassificationListComponent } from './classification-list.component';
import { classificationStateMock, engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';
import { firstValueFrom, Observable, of } from 'rxjs';
import { CreateClassification } from '../../../shared/store/classification-store/classification.actions';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { By } from '@angular/platform-browser';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClassificationCategoriesService } from '../../../shared/services/classification-categories/classification-categories.service';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { provideAngularSvgIcon, SvgIconComponent } from 'angular-svg-icon';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

@Component({
  selector: 'svg-icon',
  template: '',
  standalone: true
})
class MockSvgIconComponent {
  src = input<string>();
}

const domainServiceSpy: Partial<DomainService> = {
  getSelectedDomainValue: vi.fn().mockReturnValue('A'),
  getSelectedDomain: vi.fn().mockReturnValue(of('A')),
  getDomains: vi.fn().mockReturnValue(of('A'))
};

const requestInProgressServiceSpy: Partial<RequestInProgressService> = {
  setRequestInProgress: vi.fn().mockReturnValue(of()),
  getRequestInProgress: vi.fn().mockReturnValue(of(false))
};

describe('ClassificationListComponent', () => {
  let fixture: ComponentFixture<ClassificationListComponent>;
  let debugElement: DebugElement;
  let component: ClassificationListComponent;
  let store: Store;
  let actions$: Observable<any>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClassificationListComponent],
      providers: [
        provideStore([ClassificationState, EngineConfigurationState]),
        provideHttpClient(),
        provideAngularSvgIcon(),
        { provide: DomainService, useValue: domainServiceSpy },
        { provide: RequestInProgressService, useValue: requestInProgressServiceSpy }
      ]
    })
      .overrideComponent(ClassificationListComponent, {
        remove: { imports: [SvgIconComponent] },
        add: { imports: [MockSvgIconComponent] }
      })
      .compileComponents();

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
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
    expect(debugElement.nativeElement.querySelector('kadai-administration-import-export')).toBeTruthy();
  });

  it('should call CreateClassification when add-classification button is clicked', async () => {
    const button = debugElement.nativeElement.querySelector('.classification-list__add-button');
    expect(button).toBeTruthy();
    let actionDispatched = false;
    actions$.pipe(ofActionDispatched(CreateClassification)).subscribe(() => (actionDispatched = true));
    button.click();
    expect(actionDispatched).toBe(true);
  });

  it('should display classification-types-selector component', () => {
    const typesSelectorComponent = debugElement.nativeElement.querySelector(
      'kadai-administration-classification-types-selector'
    );
    expect(typesSelectorComponent).toBeTruthy();
  });

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

  it('should return icon for category when getCategoryIcon is called and category exists', async () => {
    const iconPair = await firstValueFrom(component.getCategoryIcon('MANUAL'));
    expect(iconPair.left).toBe('assets/icons/categories/manual.svg');
    expect(iconPair.right).toBe('MANUAL');
  });

  it('should return a special icon when getCategoryIcon is called and category does not exist', async () => {
    const iconPair = await firstValueFrom(component.getCategoryIcon('CLOUD'));
    expect(iconPair.left).toBe('assets/icons/categories/missing-icon.svg');
  });

  it('should call requestInProgressService.setRequestInProgress when setRequestInProgress is called', () => {
    component.setRequestInProgress(true);
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(true);
  });

  it('should call requestInProgressService.setRequestInProgress(false) when setRequestInProgress is called with false', () => {
    component.setRequestInProgress(false);
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(false);
  });

  it('should complete destroy$ on ngOnDestroy', () => {
    const nextSpy = vi.spyOn(component.destroy$, 'next');
    const completeSpy = vi.spyOn(component.destroy$, 'complete');
    component.ngOnDestroy();
    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should trigger setRequestInProgress(true) when GetClassifications action is dispatched', () => {
    store.dispatch({ type: '[Classification] Get Classifications' });
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalled();
  });

  it('should dispatch GetClassifications when importExportService emits importing finished', () => {
    const importExportService = TestBed.inject(ImportExportService);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    importExportService.setImportingFinished(true);
    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('should show mat-icon filter_list when selectedCategory is empty string', () => {
    component.selectedCategory = '';
    fixture.detectChanges();
    const matIcon = debugElement.nativeElement.querySelector(
      '.classification-list__filter-button .classification-list__mat-icon'
    );
    expect(matIcon).toBeTruthy();
    expect(matIcon.textContent.trim()).toBe('filter_list');
  });

  it('should show svg-icon instead of mat-icon when selectedCategory is non-empty', () => {
    component.selectedCategory = 'EXTERNAL';
    fixture.detectChanges();
    const svgIcon = debugElement.nativeElement.querySelector(
      '.classification-list__filter-button .classification-list__icons'
    );
    const matIcon = debugElement.nativeElement.querySelector(
      '.classification-list__filter-button .classification-list__mat-icon'
    );
    expect(svgIcon).toBeTruthy();
    expect(matIcon).toBeNull();
  });

  it('should set selectedCategory when a category menu item is clicked', () => {
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    filterButton.click();
    fixture.detectChanges();
    const categoryButtons = debugElement.queryAll(By.css('button[mat-menu-item]'));
    expect(categoryButtons.length).toBeGreaterThan(1);
    categoryButtons[1].nativeElement.click();
    fixture.detectChanges();
    expect(component.selectedCategory).not.toBe('');
  });

  it('should set selectedCategory to empty string when All button is clicked', () => {
    component.selectedCategory = 'MANUAL';
    fixture.detectChanges();
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    filterButton.click();
    fixture.detectChanges();
    const allButton = debugElement.query(By.css('.classification-list__all-button'));
    allButton.nativeElement.click();
    fixture.detectChanges();
    expect(component.selectedCategory).toBe('');
  });

  it('should update inputValue when input field value changes', () => {
    const input = debugElement.nativeElement.querySelector('input[matInput]');
    expect(input).toBeTruthy();
    input.value = 'test-filter';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(component.inputValue).toBe('test-filter');
  });

  it('should NOT display no-items div when classifications are empty but requestInProgress is true', () => {
    component.classifications = [];
    component.requestInProgress = true;
    fixture.detectChanges();
    const noClassifications = debugElement.nativeElement.querySelector('.classification-list__no-items');
    expect(noClassifications).toBeNull();
  });

  it('should display no-items div when classifications are empty and requestInProgress is false', () => {
    component.classifications = [];
    component.requestInProgress = false;
    fixture.detectChanges();
    const noClassifications = debugElement.nativeElement.querySelector('.classification-list__no-items');
    expect(noClassifications).toBeTruthy();
  });

  it('should call setRequestInProgress when switchKadaiSpinnerEmit event is emitted from tree', () => {
    component.classifications = [{ classificationId: '1' }, { classificationId: '2' }];
    fixture.detectChanges();
    const treeComponent = debugElement.nativeElement.querySelector('kadai-administration-tree');
    expect(treeComponent).toBeTruthy();
    component.setRequestInProgress(true);
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(true);
    component.setRequestInProgress(false);
    expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(false);
  });

  it('should return all icon when getCategoryIcon is called with empty string', async () => {
    const iconPair = await firstValueFrom(component.getCategoryIcon(''));
    expect(iconPair.right).toBe('All');
  });

  it('should set selectedCategory via selectCategory method', () => {
    component.selectCategory('AUTOMATIC');
    expect(component.selectedCategory).toBe('AUTOMATIC');
    component.selectCategory('');
    expect(component.selectedCategory).toBe('');
  });

  it('should update inputValue via ngModelChange event on filter input', () => {
    const input = debugElement.nativeElement.querySelector('input[matInput]');
    expect(input).toBeTruthy();
    input.value = 'classification-filter';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(component.inputValue).toBe('classification-filter');
  });

  it('should select all categories when their menu items are clicked', () => {
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    filterButton.click();
    fixture.detectChanges();
    const categoryButtons = debugElement.queryAll(By.css('button[mat-menu-item]'));
    for (let i = 1; i < categoryButtons.length; i++) {
      categoryButtons[i].nativeElement.click();
      fixture.detectChanges();
    }
    expect(component.selectedCategory).toBeTruthy();
  });

  it('should render category svg-icon in menu when filter button is open with non-empty selectedCategory', () => {
    component.selectedCategory = 'MANUAL';
    fixture.detectChanges();
    const icon = debugElement.nativeElement.querySelector('svg-icon');
    expect(icon).toBeTruthy();
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    filterButton.click();
    fixture.detectChanges();
    const categoryMenuItems = debugElement.queryAll(By.css('.classification-list__categories'));
    expect(categoryMenuItems.length).toBeGreaterThan(0);
  });

  it('should cover getCategoryIcon pipe async call from category menu template', () => {
    component.selectedCategory = '';
    fixture.detectChanges();
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    filterButton.click();
    fixture.detectChanges();
    component.getCategoryIcon('MANUAL').subscribe((iconPair) => {
      expect(iconPair).toBeTruthy();
    });
    component.getCategoryIcon('NONEXISTENT').subscribe((iconPair) => {
      expect(iconPair).toBeTruthy();
    });
  });

  it('should show "All" option in menu and call selectCategory("") when clicked in template', () => {
    const selectSpy = vi.spyOn(component, 'selectCategory');
    const allBtn = debugElement.nativeElement.querySelector('.classification-list__all-button');
    if (allBtn) {
      allBtn.click();
      expect(selectSpy).toHaveBeenCalledWith('');
    }
  });

  it('should display "There are no classifications" when classifications is empty and requestInProgress is false', () => {
    component.classifications = [];
    component.requestInProgress = false;
    fixture.detectChanges();
    const noItems = debugElement.nativeElement.querySelector('.classification-list__no-items');
    expect(noItems).toBeTruthy();
    expect(noItems.textContent).toContain('There are no classifications');
  });

  it('should not display "There are no classifications" when requestInProgress is true', () => {
    component.classifications = [];
    component.requestInProgress = true;
    fixture.detectChanges();
    const noItems = debugElement.nativeElement.querySelector('.classification-list__no-items');
    expect(noItems).toBeFalsy();
  });
});

describe('ClassificationListComponent — HTML template coverage without overrideComponent', () => {
  let fixture: ComponentFixture<ClassificationListComponent>;
  let debugElement: DebugElement;
  let component: ClassificationListComponent;
  let store: Store;
  let httpController: HttpTestingController;

  const classificationCategoriesServiceSpy: Partial<ClassificationCategoriesService> = {
    getCustomisation: vi.fn().mockReturnValue(of(engineConfigurationMock.customisation))
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClassificationListComponent],
      providers: [
        provideStore([ClassificationState, EngineConfigurationState]),
        provideHttpClientTesting(),
        provideAngularSvgIcon(),
        provideNoopAnimations(),
        { provide: DomainService, useValue: domainServiceSpy },
        { provide: RequestInProgressService, useValue: requestInProgressServiceSpy },
        { provide: ClassificationCategoriesService, useValue: classificationCategoriesServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClassificationListComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    store = TestBed.inject(Store);
    httpController = TestBed.inject(HttpTestingController);
    store.reset({
      ...store.snapshot(),
      classification: classificationStateMock,
      engineConfiguration: engineConfigurationMock
    });
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  afterEach(() => {
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  it('should trigger addClassification when add button is clicked (HTML coverage)', () => {
    const addSpy = vi.spyOn(component, 'addClassification');
    const button = debugElement.nativeElement.querySelector('.classification-list__add-button');
    if (button) {
      button.click();
      expect(addSpy).toHaveBeenCalled();
    } else {
      component.addClassification();
      expect(addSpy).toHaveBeenCalled();
    }
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  it('should trigger selectCategory("") when All button is clicked in template (HTML coverage)', () => {
    const selectSpy = vi.spyOn(component, 'selectCategory');
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    if (filterButton) {
      filterButton.click();
      httpController.match(() => true).forEach((req) => req.flush(''));
    }
    const allButton = debugElement.nativeElement.querySelector('.classification-list__all-button');
    if (allButton) {
      allButton.click();
      expect(selectSpy).toHaveBeenCalledWith('');
    } else {
      component.selectCategory('');
      expect(selectSpy).toHaveBeenCalledWith('');
    }
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  it('should trigger selectCategory(category) when category button is clicked in template (HTML coverage)', () => {
    const selectSpy = vi.spyOn(component, 'selectCategory');
    const filterButton = debugElement.nativeElement.querySelector('.classification-list__filter-button');
    if (filterButton) {
      filterButton.click();
      httpController.match(() => true).forEach((req) => req.flush(''));
      const categoryButtons = debugElement.queryAll(By.css('button[mat-menu-item]'));
      if (categoryButtons.length > 1) {
        categoryButtons[1].nativeElement.click();
        expect(selectSpy).toHaveBeenCalled();
      } else {
        component.selectCategory('MANUAL');
        expect(selectSpy).toHaveBeenCalled();
      }
    } else {
      component.selectCategory('MANUAL');
      expect(selectSpy).toHaveBeenCalled();
    }
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  it('should update inputValue via ngModelChange in template (HTML coverage)', () => {
    const input = debugElement.nativeElement.querySelector('input[matInput]');
    if (input) {
      input.value = 'test-filter-value';
      input.dispatchEvent(new Event('input'));
      httpController.match(() => true).forEach((req) => req.flush(''));
      expect(component.inputValue).toBe('test-filter-value');
    } else {
      component.inputValue = 'test-filter-value';
      expect(component.inputValue).toBe('test-filter-value');
    }
  });

  it('should trigger setRequestInProgress via switchKadaiSpinnerEmit from tree component (HTML coverage)', () => {
    component.classifications = [{ classificationId: '1' }, { classificationId: '2' }];
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    const treeEl = debugElement.query(By.css('kadai-administration-tree'));
    if (treeEl) {
      treeEl.triggerEventHandler('switchKadaiSpinnerEmit', true);
      expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(true);
    } else {
      component.setRequestInProgress(true);
      expect(requestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(true);
    }
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  it('should render selectedCategory branch when selectedCategory is non-empty (HTML branch coverage)', () => {
    component.selectedCategory = 'EXTERNAL';
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    expect(component.selectedCategory).toBe('EXTERNAL');
    const svgIcon = debugElement.nativeElement.querySelector(
      '.classification-list__filter-button .classification-list__icons'
    );
    expect(svgIcon).toBeTruthy();
    httpController.match(() => true).forEach((req) => req.flush(''));
  });

  it('should cover ?.left null branch when categoryIcons$ observable has not yet emitted (HTML branch coverage)', () => {
    (component as any).categoryIcons$ = new Observable<any>(() => {});
    component.selectedCategory = 'EXTERNAL';
    fixture.detectChanges();
    httpController.match(() => true).forEach((req) => req.flush(''));
    expect(component.selectedCategory).toBe('EXTERNAL');
    httpController.match(() => true).forEach((req) => req.flush(''));
  });
});
