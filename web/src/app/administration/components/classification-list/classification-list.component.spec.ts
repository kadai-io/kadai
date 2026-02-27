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
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideAngularSvgIcon } from 'angular-svg-icon';

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
    }).compileComponents();

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
});
