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
import { SimpleChange } from '@angular/core';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, Subject } from 'rxjs';
import { provideStore, Store } from '@ngxs/store';

import { TaskInformationComponent } from './task-information.component';
import { Task } from '../../models/task';
import { ObjectReference } from '../../models/object-reference';
import { Classification } from '../../../shared/models/classification';
import { AccessId } from '../../../shared/models/access-id';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { FormsValidatorService } from '../../../shared/services/forms-validator/forms-validator.service';
import { EngineConfigurationState } from '../../../shared/store/engine-configuration-store/engine-configuration.state';
import { engineConfigurationMock } from '../../../shared/store/mock-data/mock-store';

const mockPrimaryObjRef = new ObjectReference(undefined, 'Company A', 'System A', 'Instance A', 'TypeA', 'Value A');

const mockTask = new Task(
  'task-1',
  mockPrimaryObjRef,
  { workbasketId: 'wb-1', name: 'Test WB', domain: 'DOMAIN_A' },
  { classificationId: 'class-1' },
  undefined,
  undefined,
  'owner1',
  undefined,
  undefined,
  undefined,
  undefined,
  undefined,
  undefined,
  '2026-01-15T10:00:00Z',
  'Test Task'
);

describe('TaskInformationComponent', () => {
  let component: TaskInformationComponent;
  let fixture: ComponentFixture<TaskInformationComponent>;
  let inputOverflowSubject: Subject<Map<string, boolean>>;

  let mockClassificationsService: {
    getClassifications: ReturnType<typeof vi.fn>;
  };

  let mockFormsValidatorService: {
    inputOverflowObservable: Subject<Map<string, boolean>>;
    validateInputOverflow: ReturnType<typeof vi.fn>;
    isFieldValid: ReturnType<typeof vi.fn>;
    validateFormInformation: ReturnType<typeof vi.fn>;
    formSubmitAttempt: boolean;
  };

  beforeEach(async () => {
    inputOverflowSubject = new Subject<Map<string, boolean>>();

    mockClassificationsService = {
      getClassifications: vi.fn().mockReturnValue(
        of({
          classifications: [{ classificationId: 'class-1' }, { classificationId: 'class-2' }]
        })
      )
    };

    mockFormsValidatorService = {
      inputOverflowObservable: inputOverflowSubject,
      validateInputOverflow: vi.fn(),
      isFieldValid: vi.fn().mockReturnValue(true),
      validateFormInformation: vi.fn().mockResolvedValue(true),
      formSubmitAttempt: false
    };

    await TestBed.configureTestingModule({
      imports: [TaskInformationComponent],
      providers: [
        provideStore([EngineConfigurationState]),
        provideNoopAnimations(),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ClassificationsService, useValue: mockClassificationsService },
        { provide: FormsValidatorService, useValue: mockFormsValidatorService }
      ]
    }).compileComponents();

    const store = TestBed.inject(Store);
    store.reset({ ...store.snapshot(), engineConfiguration: engineConfigurationMock });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskInformationComponent);
    component = fixture.componentInstance;
    component.task = { ...mockTask };
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit()', () => {
    it('should call getClassifications on ClassificationsService with the task domain', () => {
      expect(mockClassificationsService.getClassifications).toHaveBeenCalledWith({
        domain: ['DOMAIN_A']
      });
    });

    it('should set requestInProgress to false after classifications are loaded', () => {
      expect(component.requestInProgress).toBe(false);
    });

    it('should populate classifications from the service response', () => {
      expect(component.classifications).toBeDefined();
      expect(component.classifications.length).toBe(2);
    });

    it('should update inputOverflowMap when inputOverflowObservable emits', () => {
      const newMap = new Map<string, boolean>([['fieldA', true]]);
      inputOverflowSubject.next(newMap);

      expect(component.inputOverflowMap).toBe(newMap);
    });

    it('should update inputOverflowMap on subsequent emissions', () => {
      const firstMap = new Map<string, boolean>([['field1', true]]);
      const secondMap = new Map<string, boolean>([['field2', false]]);

      inputOverflowSubject.next(firstMap);
      expect(component.inputOverflowMap).toBe(firstMap);

      inputOverflowSubject.next(secondMap);
      expect(component.inputOverflowMap).toBe(secondMap);
    });

    it('should assign validateInputOverflow as a function', () => {
      expect(typeof component.validateInputOverflow).toBe('function');
    });

    it('should call formsValidatorService.validateInputOverflow when validateInputOverflow arrow function is invoked', () => {
      const fakeModel = { name: 'taskName', value: 'some-value' } as any;
      const maxLength = 100;

      component.validateInputOverflow(fakeModel, maxLength);

      expect(mockFormsValidatorService.validateInputOverflow).toHaveBeenCalledWith(fakeModel, maxLength);
    });
  });

  describe('ngOnChanges()', () => {
    it('should call validate (and thus validateFormInformation) when saveToggleTriggered changes value', async () => {
      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(false, true, false)
      });

      await fixture.whenStable();

      expect(mockFormsValidatorService.validateFormInformation).toHaveBeenCalled();
    });

    it('should set formSubmitAttempt to true when saveToggleTriggered changes value', () => {
      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(false, true, false)
      });

      expect(mockFormsValidatorService.formSubmitAttempt).toBe(true);
    });

    it('should not call validateFormInformation when saveToggleTriggered value stays the same', () => {
      mockFormsValidatorService.validateFormInformation.mockClear();

      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(true, true, false)
      });

      expect(mockFormsValidatorService.validateFormInformation).not.toHaveBeenCalled();
    });

    it('should not call validateFormInformation when saveToggleTriggered is not in changes', () => {
      mockFormsValidatorService.validateFormInformation.mockClear();

      component.ngOnChanges({});

      expect(mockFormsValidatorService.validateFormInformation).not.toHaveBeenCalled();
    });
  });

  describe('isFieldValid()', () => {
    it('should delegate to formsValidatorService.isFieldValid with taskForm and field name', () => {
      const result = component.isFieldValid('taskName');

      expect(mockFormsValidatorService.isFieldValid).toHaveBeenCalledWith(component.taskForm, 'taskName');
      expect(result).toBe(true);
    });

    it('should return false when formsValidatorService.isFieldValid returns false', () => {
      mockFormsValidatorService.isFieldValid.mockReturnValue(false);

      const result = component.isFieldValid('missingField');

      expect(result).toBe(false);
    });
  });

  describe('updateDate()', () => {
    it('should update task.due with the ISO string of the new date', () => {
      const newDate = new Date('2026-03-10T12:00:00Z');
      component.updateDate({ value: newDate });

      expect(component.task.due).toBe(newDate.toISOString());
    });

    it('should not update task.due when the event value is null', () => {
      const originalDue = component.task.due;
      component.updateDate({ value: null });

      expect(component.task.due).toBe(originalDue);
    });

    it('should not update task.due when the event value is undefined', () => {
      const originalDue = component.task.due;
      component.updateDate({ value: undefined });

      expect(component.task.due).toBe(originalDue);
    });
  });

  describe('changedClassification()', () => {
    it('should update task.classificationSummary with the selected classification', () => {
      const classification: Classification = { classificationId: 'class-42', name: 'New Class' } as Classification;

      component.changedClassification(classification);

      expect(component.task.classificationSummary).toBe(classification);
    });

    it('should set isClassificationEmpty to false', () => {
      component.isClassificationEmpty = true;
      const classification: Classification = { classificationId: 'class-42' } as Classification;

      component.changedClassification(classification);

      expect(component.isClassificationEmpty).toBe(false);
    });
  });

  describe('onSelectedOwner()', () => {
    it('should set task.owner to the accessId of the provided owner', () => {
      const owner: AccessId = { accessId: 'user-99', name: 'Test User' };

      component.onSelectedOwner(owner);

      expect(component.task.owner).toBe('user-99');
    });

    it('should not update task.owner when owner is null', () => {
      const originalOwner = component.task.owner;

      component.onSelectedOwner(null);

      expect(component.task.owner).toBe(originalOwner);
    });

    it('should not update task.owner when owner has no accessId', () => {
      const originalOwner = component.task.owner;

      component.onSelectedOwner({ name: 'No ID User' });

      expect(component.task.owner).toBe(originalOwner);
    });
  });

  describe('ngOnDestroy()', () => {
    it('should call next and complete on the internal destroy$ subject', () => {
      const nextSpy = vi.spyOn(component['destroy$'], 'next');
      const completeSpy = vi.spyOn(component['destroy$'], 'complete');

      component.ngOnDestroy();

      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });

    it('should stop updating inputOverflowMap after destroy', () => {
      component.ngOnDestroy();

      const mapBeforeEmit = component.inputOverflowMap;
      const newMap = new Map<string, boolean>([['field', true]]);
      inputOverflowSubject.next(newMap);

      expect(component.inputOverflowMap).toBe(mapBeforeEmit);
    });

    it('should stop updating classifications after destroy', () => {
      const classificationSubject = new Subject<any>();
      mockClassificationsService.getClassifications.mockReturnValue(classificationSubject.asObservable());

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newComponent.task = { ...mockTask };
      newFixture.detectChanges();

      const classificationsBefore = newComponent.classifications;

      newComponent.ngOnDestroy();

      classificationSubject.next({ classifications: [{ classificationId: 'new-class' }] });

      expect(newComponent.classifications).toBe(classificationsBefore);
    });
  });

  describe('validate() - triggered via ngOnChanges', () => {
    it('should emit formValid(true) when form is valid, classification is set, and owner is valid', async () => {
      mockFormsValidatorService.validateFormInformation.mockResolvedValue(true);
      component.isOwnerValid = true;
      component.task.classificationSummary = { classificationId: 'class-1' };

      const emittedValues: boolean[] = [];
      component.formValid.subscribe((val) => emittedValues.push(val));

      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(false, true, false)
      });

      await fixture.whenStable();

      expect(emittedValues).toContain(true);
    });

    it('should not emit formValid when form validation returns false', async () => {
      mockFormsValidatorService.validateFormInformation.mockResolvedValue(false);
      component.task.classificationSummary = { classificationId: 'class-1' };

      const emittedValues: boolean[] = [];
      component.formValid.subscribe((val) => emittedValues.push(val));

      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(false, true, false)
      });

      await fixture.whenStable();

      expect(emittedValues.length).toBe(0);
    });

    it('should not emit formValid when classificationSummary is undefined', async () => {
      mockFormsValidatorService.validateFormInformation.mockResolvedValue(true);
      component.isOwnerValid = true;
      component.task.classificationSummary = undefined;

      const emittedValues: boolean[] = [];
      component.formValid.subscribe((val) => emittedValues.push(val));

      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(false, true, false)
      });

      await fixture.whenStable();

      expect(emittedValues.length).toBe(0);
    });

    it('should not emit formValid when isOwnerValid is false', async () => {
      mockFormsValidatorService.validateFormInformation.mockResolvedValue(true);
      component.isOwnerValid = false;
      component.task.classificationSummary = { classificationId: 'class-1' };

      const emittedValues: boolean[] = [];
      component.formValid.subscribe((val) => emittedValues.push(val));

      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(false, true, false)
      });

      await fixture.whenStable();

      expect(emittedValues.length).toBe(0);
    });

    it('should set isClassificationEmpty to true when classificationSummary is undefined', () => {
      component.task.classificationSummary = undefined;

      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(false, true, false)
      });

      expect(component.isClassificationEmpty).toBe(true);
    });

    it('should set isClassificationEmpty to false when classificationSummary is defined', () => {
      component.task.classificationSummary = { classificationId: 'class-1' };

      component.ngOnChanges({
        saveToggleTriggered: new SimpleChange(false, true, false)
      });

      expect(component.isClassificationEmpty).toBe(false);
    });
  });

  describe('getClassificationByDomain() - triggered via ngOnInit', () => {
    it('should set requestInProgress to true before the HTTP call resolves', () => {
      const classificationSubject = new Subject<any>();
      mockClassificationsService.getClassifications.mockReturnValue(classificationSubject.asObservable());

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newComponent.task = { ...mockTask };

      newFixture.detectChanges();

      expect(newComponent.requestInProgress).toBe(true);
    });

    it('should assign classifications from classificationPagingList.classifications', () => {
      const expectedClassifications: Classification[] = [
        { classificationId: 'class-A' } as Classification,
        { classificationId: 'class-B' } as Classification
      ];
      mockClassificationsService.getClassifications.mockReturnValue(of({ classifications: expectedClassifications }));

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newComponent.task = { ...mockTask };
      newFixture.detectChanges();

      expect(newComponent.classifications).toEqual(expectedClassifications);
    });

    it('should set requestInProgress to false after classifications are returned', () => {
      const classificationSubject = new Subject<any>();
      mockClassificationsService.getClassifications.mockReturnValue(classificationSubject.asObservable());

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newComponent.task = { ...mockTask };
      newFixture.detectChanges();

      classificationSubject.next({ classifications: [] });

      expect(newComponent.requestInProgress).toBe(false);
    });

    it('should pass the task workbasket domain to getClassifications', () => {
      mockClassificationsService.getClassifications.mockClear();
      mockClassificationsService.getClassifications.mockReturnValue(of({ classifications: [] }));

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newComponent.task = {
        ...mockTask,
        workbasketSummary: { workbasketId: 'wb-2', name: 'WB2', domain: 'DOMAIN_B' }
      };
      newFixture.detectChanges();

      expect(mockClassificationsService.getClassifications).toHaveBeenCalledWith({ domain: ['DOMAIN_B'] });
    });
  });
});
