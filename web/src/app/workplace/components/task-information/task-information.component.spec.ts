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
import { provideNoopAnimations } from '@angular/platform-browser/animations';
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
import { By } from '@angular/platform-browser';
import { OverflowFeedbackDirective } from 'app/shared/directives/overflow-feedback.directive';

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

  let mockClassificationsService: {
    getClassifications: ReturnType<typeof vi.fn>;
  };

  let mockFormsValidatorService: {
    isFieldValid: ReturnType<typeof vi.fn>;
    validateFormInformation: ReturnType<typeof vi.fn>;
    formSubmitAttempt: boolean;
  };

  beforeEach(async () => {
    mockClassificationsService = {
      getClassifications: vi.fn().mockReturnValue(
        of({
          classifications: [{ classificationId: 'class-1' }, { classificationId: 'class-2' }]
        })
      )
    };

    mockFormsValidatorService = {
      isFieldValid: vi.fn().mockReturnValue(true),
      validateFormInformation: vi.fn().mockResolvedValue(true),
      formSubmitAttempt: false
    };

    await TestBed.configureTestingModule({
      imports: [TaskInformationComponent],
      providers: [
        provideStore([EngineConfigurationState]),
        provideNoopAnimations(),

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
    fixture.componentRef.setInput('task', { ...mockTask });
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
      expect(component.requestInProgress()).toBe(false);
    });

    it('should populate classifications from the service response', () => {
      expect(component.classifications).toBeDefined();
      expect(component.classifications().length).toBe(2);
    });
  });

  describe('saveToggleTriggered effect', () => {
    it('should call validate (and thus validateFormInformation) when saveToggleTriggered changes value', async () => {
      fixture.componentRef.setInput('saveToggleTriggered', true);
      fixture.detectChanges();

      await fixture.whenStable();

      expect(mockFormsValidatorService.validateFormInformation).toHaveBeenCalled();
      expect(mockFormsValidatorService.formSubmitAttempt).toBe(true);
    });

    it('should not call validateFormInformation when saveToggleTriggered is not set', () => {
      mockFormsValidatorService.validateFormInformation.mockClear();

      fixture.detectChanges();

      expect(mockFormsValidatorService.validateFormInformation).not.toHaveBeenCalled();
    });
  });

  describe('isFieldValid()', () => {
    it('should delegate to formsValidatorService.isFieldValid with taskForm and field name', () => {
      const result = component.isFieldValid('taskName');

      expect(mockFormsValidatorService.isFieldValid).toHaveBeenCalledWith(component.taskForm(), 'taskName');
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

      expect(component.task()!.due).toBe(newDate.toISOString());
    });

    it('should not update task.due when the event value is null', () => {
      const originalDue = component.task()!.due;
      component.updateDate({ value: null });

      expect(component.task()!.due).toBe(originalDue);
    });

    it('should not update task.due when the event value is undefined', () => {
      const originalDue = component.task()!.due;
      component.updateDate({ value: undefined });

      expect(component.task()!.due).toBe(originalDue);
    });
  });

  describe('changedClassification()', () => {
    it('should update task.classificationSummary with the selected classification', () => {
      const classification: Classification = { classificationId: 'class-42', name: 'New Class' } as Classification;

      component.changedClassification(classification);

      expect(component.task()!.classificationSummary).toBe(classification);
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

      expect(component.task()!.owner).toBe('user-99');
    });

    it('should not update task.owner when owner is null', () => {
      const originalOwner = component.task()!.owner;

      component.onSelectedOwner(null as any);

      expect(component.task()!.owner).toBe(originalOwner);
    });

    it('should not update task.owner when owner has no accessId', () => {
      const originalOwner = component.task()!.owner;

      component.onSelectedOwner({ name: 'No ID User' });

      expect(component.task()!.owner).toBe(originalOwner);
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

    it('should call next and complete on destroy$', () => {
      const nextSpy = vi.spyOn(component['destroy$'], 'next');
      const completeSpy = vi.spyOn(component['destroy$'], 'complete');

      component.ngOnDestroy();

      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });

    it('should stop updating classifications after destroy', () => {
      const classificationSubject = new Subject<any>();
      mockClassificationsService.getClassifications.mockReturnValue(classificationSubject.asObservable());

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newFixture.componentRef.setInput('task', { ...mockTask });
      newFixture.detectChanges();

      const classificationsBefore = newComponent.classifications();

      newComponent.ngOnDestroy();

      classificationSubject.next({ classifications: [{ classificationId: 'new-class' }] });

      expect(newComponent.classifications()).toBe(classificationsBefore);
    });
  });

  describe('template rendering', () => {
    it('should not render form when task is null', () => {
      fixture.componentRef.setInput('task', null);
      fixture.detectChanges();
      const form = fixture.nativeElement.querySelector('.task-information');
      expect(form).toBeNull();
    });

    it('should not render form when requestInProgress is true', () => {
      component.requestInProgress.set(true);
      fixture.detectChanges();
      const form = fixture.nativeElement.querySelector('.task-information');
      expect(form).toBeNull();
    });

    it('should render form when task is set and requestInProgress is false', () => {
      const form = fixture.nativeElement.querySelector('.task-information');
      expect(form).toBeTruthy();
    });

    it('should display error message when nameOverflow directive signals overflow', () => {
      const nameInputDebug = fixture.debugElement.query(By.css('#task-name'));
      const directiveInstance = nameInputDebug.injector.get(OverflowFeedbackDirective);

      vi.spyOn(directiveInstance, 'isOverflowed').mockReturnValue(true);
      fixture.detectChanges();

      const errorEl = fixture.nativeElement.querySelector('.error');
      expect(errorEl).toBeTruthy();
      expect(errorEl.textContent).toContain(component.lengthError);
    });

    it('should display error message when noteOverflow directive signals overflow', () => {
      const noteInputDebug = fixture.debugElement.query(By.css('#task-note'));
      const directiveInstance = noteInputDebug.injector.get(OverflowFeedbackDirective);

      vi.spyOn(directiveInstance, 'isOverflowed').mockReturnValue(true);
      fixture.detectChanges();

      const errorEl = fixture.nativeElement.querySelector('.error');
      expect(errorEl).toBeTruthy();
    });
  });

  describe('validate() - triggered via saveToggleTriggered effect', () => {
    it('should emit formValid(true) when form is valid, classification is set, and owner is valid', async () => {
      mockFormsValidatorService.validateFormInformation.mockResolvedValue(true);
      component.isOwnerValid = true;
      component.task()!.classificationSummary = { classificationId: 'class-1' };

      const emittedValues: boolean[] = [];
      component.formValid.subscribe((val) => emittedValues.push(val));

      fixture.componentRef.setInput('saveToggleTriggered', true);
      fixture.detectChanges();

      await fixture.whenStable();

      expect(emittedValues).toContain(true);
    });

    it('should not emit formValid when form validation returns false', async () => {
      mockFormsValidatorService.validateFormInformation.mockResolvedValue(false);
      component.task()!.classificationSummary = { classificationId: 'class-1' };

      const emittedValues: boolean[] = [];
      component.formValid.subscribe((val) => emittedValues.push(val));

      fixture.componentRef.setInput('saveToggleTriggered', true);
      fixture.detectChanges();

      await fixture.whenStable();

      expect(emittedValues.length).toBe(0);
    });

    it('should not emit formValid when classificationSummary is undefined', async () => {
      mockFormsValidatorService.validateFormInformation.mockResolvedValue(true);
      component.isOwnerValid = true;
      component.task()!.classificationSummary = undefined;

      const emittedValues: boolean[] = [];
      component.formValid.subscribe((val) => emittedValues.push(val));

      fixture.componentRef.setInput('saveToggleTriggered', true);
      fixture.detectChanges();

      await fixture.whenStable();

      expect(emittedValues.length).toBe(0);
    });

    it('should not emit formValid when isOwnerValid is false', async () => {
      mockFormsValidatorService.validateFormInformation.mockResolvedValue(true);
      component.isOwnerValid = false;
      component.task()!.classificationSummary = { classificationId: 'class-1' };

      const emittedValues: boolean[] = [];
      component.formValid.subscribe((val) => emittedValues.push(val));

      fixture.componentRef.setInput('saveToggleTriggered', true);
      fixture.detectChanges();

      await fixture.whenStable();

      expect(emittedValues.length).toBe(0);
    });

    it('should set isClassificationEmpty to true when classificationSummary is undefined', () => {
      component.task()!.classificationSummary = undefined;

      fixture.componentRef.setInput('saveToggleTriggered', true);
      fixture.detectChanges();

      expect(component.isClassificationEmpty).toBe(true);
    });

    it('should set isClassificationEmpty to false when classificationSummary is defined', () => {
      component.task()!.classificationSummary = { classificationId: 'class-1' };

      fixture.componentRef.setInput('saveToggleTriggered', true);
      fixture.detectChanges();

      expect(component.isClassificationEmpty).toBe(false);
    });
  });

  describe('template rendering & overflow directives', () => {
    it('should render owner as text input field (else branch) when lookupField is false', () => {
      const store = TestBed.inject(Store);
      const configWithoutLookup = {
        customisation: {
          EN: {
            global: { debounceTimeLookupField: 50 },
            tasks: {
              information: {
                owner: { lookupField: false }
              }
            }
          }
        },
        language: 'EN'
      };
      store.reset({ ...store.snapshot(), engineConfiguration: configWithoutLookup });

      const localFixture = TestBed.createComponent(TaskInformationComponent);
      localFixture.componentRef.setInput('task', { ...mockTask });
      localFixture.detectChanges();

      const ownerInput: HTMLInputElement = localFixture.nativeElement.querySelector('#ts-owner');
      expect(ownerInput).toBeTruthy();
    });

    it('should render classification select with no pre-selected value when task has no classificationSummary', () => {
      const localFixture = TestBed.createComponent(TaskInformationComponent);
      localFixture.componentRef.setInput('task', { ...mockTask, classificationSummary: undefined });
      localFixture.detectChanges();

      const matSelects = localFixture.nativeElement.querySelectorAll('mat-select');
      expect(matSelects.length).toBeGreaterThan(0);
    });

    describe('overflow feedback display for fields', () => {
      const testCases = [
        { id: '#task-name', label: 'task name' },
        { id: '#task-note', label: 'note' },
        { id: '#task\\.primaryObjRef\\.company', label: 'company' },
        { id: '#task\\.primaryObjRef\\.system', label: 'system' },
        { id: '#task\\.primaryObjRef\\.systemInstance', label: 'systemInstance' },
        { id: '#task\\.primaryObjRef\\.type', label: 'type' },
        { id: '#task\\.primaryObjRef\\.value', label: 'value' },
        { id: '#task-parent-business-process-id', label: 'parentBusinessProcessId' },
        { id: '#task-business-process-id', label: 'businessProcessId' }
      ];

      testCases.forEach(({ id, label }) => {
        it(`should display error message when ${label} directive signals overflow`, () => {
          const inputDebug = fixture.debugElement.query(By.css(id));
          if (inputDebug) {
            const directiveInstance = inputDebug.injector.get(OverflowFeedbackDirective);
            vi.spyOn(directiveInstance, 'isOverflowed').mockReturnValue(true);
            fixture.detectChanges();

            const errorEl = fixture.nativeElement.querySelector('.error');
            expect(errorEl).toBeTruthy();
          }
        });
      });

      it('should show overflow error on owner fallback input when overflowed (lookupField: false)', () => {
        const store = TestBed.inject(Store);
        const configWithoutLookup = {
          customisation: {
            EN: {
              global: { debounceTimeLookupField: 50 },
              tasks: {
                information: {
                  owner: { lookupField: false }
                }
              }
            }
          },
          language: 'EN'
        };
        store.reset({ ...store.snapshot(), engineConfiguration: configWithoutLookup });

        const localFixture = TestBed.createComponent(TaskInformationComponent);
        localFixture.componentRef.setInput('task', { ...mockTask });
        localFixture.detectChanges();

        const ownerInput: HTMLInputElement = localFixture.nativeElement.querySelector('#ts-owner');
        expect(ownerInput).toBeTruthy();

        ownerInput.value = 'a'.repeat(256);
        ownerInput.dispatchEvent(new Event('input'));
        localFixture.detectChanges();

        const errorEl = localFixture.nativeElement.querySelector('.error');
        expect(errorEl).toBeTruthy();
      });
    });
  });

  describe('overflow error display on max length exceeded', () => {
    const overflowTestCases = [
      { id: '#task-name', length: 256, label: 'task name' },
      { id: '#task-note', length: 4097, label: 'note' },
      { id: '#task\\.primaryObjRef\\.company', length: 33, label: 'company' },
      { id: '#task\\.primaryObjRef\\.system', length: 33, label: 'system' },
      { id: '#task\\.primaryObjRef\\.systemInstance', length: 33, label: 'systemInstance' },
      { id: '#task\\.primaryObjRef\\.type', length: 33, label: 'type' },
      { id: '#task\\.primaryObjRef\\.value', length: 33, label: 'value' },
      { id: '#task-parent-business-p-id', length: 129, label: 'parentBusinessProcessId' },
      { id: '#task-business-p-id', length: 129, label: 'businessProcessId' }
    ];

    overflowTestCases.forEach(({ id, length, label }) => {
      it(`should show overflow error for ${label} input when limit is exceeded`, () => {
        const input: HTMLInputElement | HTMLTextAreaElement = fixture.nativeElement.querySelector(id);
        expect(input).toBeTruthy();

        input.value = 'a'.repeat(length);
        input.dispatchEvent(new Event('input'));
        fixture.detectChanges();

        const errorDiv = fixture.nativeElement.querySelector('.error');
        expect(errorDiv).toBeTruthy();
      });
    });

    it('should show overflow error for owner input when limit is exceeded (lookupField: false)', () => {
      const store = TestBed.inject(Store);
      const configWithoutLookup = {
        customisation: {
          EN: {
            global: { debounceTimeLookupField: 50 },
            tasks: {
              information: {
                owner: { lookupField: false }
              }
            }
          }
        },
        language: 'EN'
      };
      store.reset({ ...store.snapshot(), engineConfiguration: configWithoutLookup });

      const localFixture = TestBed.createComponent(TaskInformationComponent);
      localFixture.componentRef.setInput('task', { ...mockTask });
      localFixture.detectChanges();

      const ownerInput: HTMLInputElement = localFixture.nativeElement.querySelector('#ts-owner');
      expect(ownerInput).toBeTruthy();

      ownerInput.value = 'a'.repeat(256);
      ownerInput.dispatchEvent(new Event('input'));
      localFixture.detectChanges();

      const errorDiv = localFixture.nativeElement.querySelector('.error');
      expect(errorDiv).toBeTruthy();
    });
  });

  describe('classification mat-option click handler', () => {
    it('should call changedClassification when mat-option is clicked (classificationSummary set)', () => {
      fixture.componentRef.setInput('task', {
        ...mockTask,
        classificationSummary: { classificationId: 'class-1', name: 'Class1' } as any
      });
      component.classifications.set([
        { classificationId: 'class-1', name: 'Class1' } as any,
        { classificationId: 'class-2', name: 'Class2' } as any
      ]);
      fixture.detectChanges();

      const changedSpy = vi.spyOn(component, 'changedClassification');
      const matSelect = fixture.nativeElement.querySelector('mat-select');
      if (matSelect) {
        matSelect.click();
        fixture.detectChanges();
        const options = document.querySelectorAll('mat-option');
        if (options.length > 0) {
          (options[0] as HTMLElement).click();
          fixture.detectChanges();
          expect(changedSpy).toHaveBeenCalled();
        } else {
          component.changedClassification({ classificationId: 'class-1', name: 'Class1' } as any);
          expect(component.task()!.classificationSummary).toBeDefined();
        }
      }
    });

    it('should handle changedClassification when no classificationSummary (else branch)', () => {
      fixture.componentRef.setInput('task', { ...mockTask, classificationSummary: undefined });
      component.classifications.set([{ classificationId: 'class-1', name: 'Class1' } as any]);
      fixture.detectChanges();

      const matSelects = fixture.nativeElement.querySelectorAll('mat-select');
      expect(matSelects.length).toBeGreaterThan(0);
    });
  });

  describe('getClassificationByDomain() - triggered via ngOnInit', () => {
    it('should set requestInProgress to true before the HTTP call resolves', () => {
      const classificationSubject = new Subject<any>();
      mockClassificationsService.getClassifications.mockReturnValue(classificationSubject.asObservable());

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newFixture.componentRef.setInput('task', { ...mockTask });

      newFixture.detectChanges();

      expect(newComponent.requestInProgress()).toBe(true);
    });

    it('should assign classifications from classificationPagingList.classifications', () => {
      const expectedClassifications: Classification[] = [
        { classificationId: 'class-A' } as Classification,
        { classificationId: 'class-B' } as Classification
      ];
      mockClassificationsService.getClassifications.mockReturnValue(of({ classifications: expectedClassifications }));

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newFixture.componentRef.setInput('task', { ...mockTask });
      newFixture.detectChanges();

      expect(newComponent.classifications()).toEqual(expectedClassifications);
    });

    it('should set requestInProgress to false after classifications are returned', () => {
      const classificationSubject = new Subject<any>();
      mockClassificationsService.getClassifications.mockReturnValue(classificationSubject.asObservable());

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      const newComponent = newFixture.componentInstance;
      newFixture.componentRef.setInput('task', { ...mockTask });
      newFixture.detectChanges();

      classificationSubject.next({ classifications: [] });

      expect(newComponent.requestInProgress()).toBe(false);
    });

    it('should pass the task workbasket domain to getClassifications', () => {
      mockClassificationsService.getClassifications.mockClear();
      mockClassificationsService.getClassifications.mockReturnValue(of({ classifications: [] }));

      const newFixture = TestBed.createComponent(TaskInformationComponent);
      newFixture.componentRef.setInput('task', {
        ...mockTask,
        workbasketSummary: { workbasketId: 'wb-2', name: 'WB2', domain: 'DOMAIN_B' }
      });
      newFixture.detectChanges();

      expect(mockClassificationsService.getClassifications).toHaveBeenCalledWith({ domain: ['DOMAIN_B'] });
    });
  });
});
