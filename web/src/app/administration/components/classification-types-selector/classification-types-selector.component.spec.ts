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

import { ClassificationTypesSelectorComponent } from './classification-types-selector.component';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { provideStore, Store } from '@ngxs/store';
import { ClassificationState } from '../../../shared/store/classification-store/classification.state';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { ClassificationCategoriesService } from '../../../shared/services/classification-categories/classification-categories.service';
import { DomainService } from '../../../shared/services/domain/domain.service';
import { classificationStateMock } from '../../../shared/store/mock-data/mock-store';
import { By } from '@angular/platform-browser';

const classificationServiceSpy = jest.fn();
const classificationCategoriesServiceSpy = jest.fn();
const domainServiceSpy = jest.fn();

describe('ClassificationTypesSelectorComponent', () => {
  let fixture: ComponentFixture<ClassificationTypesSelectorComponent>;
  let debugElement: DebugElement;
  let component: ClassificationTypesSelectorComponent;
  let store: Store;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ClassificationTypesSelectorComponent],
      providers: [
        provideStore([ClassificationState]),
        {
          provide: ClassificationsService,
          useValue: classificationServiceSpy
        },
        {
          provide: ClassificationCategoriesService,
          useValue: classificationCategoriesServiceSpy
        },
        { provide: DomainService, useValue: domainServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClassificationTypesSelectorComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    store = TestBed.inject(Store);
    store.reset({
      ...store.snapshot(),
      classification: classificationStateMock
    });
    fixture.detectChanges();
  }));

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should display form-field for types-selector', () => {
    const button = debugElement.nativeElement.getElementsByClassName('types-selector');
    expect(button).toBeTruthy();
  });

  it('should display selected classification type', () => {
    fixture.detectChanges();
    const button = debugElement.nativeElement.querySelector('.types-selector__selected-type');
    expect(button.textContent).toBe('DOCUMENT');
  });

  it('should display dropdown with 2 objects', () => {
    const dropdownButton = debugElement.nativeElement.querySelector('.types-selector__selected-type');
    expect(dropdownButton).toBeTruthy();
    dropdownButton.click();
    fixture.detectChanges();
    const options = debugElement.queryAll(By.css('.types-selector__options'));
    expect(options.length).toBe(2);
    expect(options[0].nativeElement.textContent.trim()).toBe('TASK');
    expect(options[1].nativeElement.textContent.trim()).toBe('DOCUMENT');
  });
});
