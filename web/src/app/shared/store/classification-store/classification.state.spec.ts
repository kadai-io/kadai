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

import { TestBed } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';

import { ClassificationState } from './classification.state';
import {
  CopyClassification,
  CreateClassification,
  DeselectClassification,
  GetClassifications,
  RemoveSelectedClassification,
  RestoreSelectedClassification,
  SaveCreatedClassification,
  SaveModifiedClassification,
  SelectClassification,
  SetSelectedClassificationType,
  UpdateClassification
} from './classification.actions';

import { ClassificationCategoriesService } from '../../services/classification-categories/classification-categories.service';
import { ClassificationsService } from '../../services/classifications/classifications.service';
import { DomainService } from '../../services/domain/domain.service';

const mockClassificationTypes = {
  TASK: ['EXTERNAL', 'MANUAL', 'AUTOMATIC'],
  DOCUMENT: ['EXTERNAL', 'MANUAL']
};

const mockClassification = {
  classificationId: 'CLI:001',
  key: 'TEST_KEY',
  name: 'Test Classification',
  type: 'TASK',
  category: 'EXTERNAL',
  domain: 'DOMAIN_A',
  created: '2020-01-01T00:00:00.000Z',
  modified: '2020-01-01T00:00:00.000Z',
  parentId: '',
  parentKey: ''
};

const mockClassificationList = {
  classifications: [
    { classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Classification 1', type: 'TASK' },
    { classificationId: 'CLI:002', key: 'TEST_KEY_2', name: 'Classification 2', type: 'TASK' }
  ]
};

const initialClassificationState = {
  classifications: mockClassificationList.classifications,
  selectedClassification: mockClassification,
  selectedClassificationType: 'TASK',
  classificationTypes: mockClassificationTypes,
  badgeMessage: ''
};

describe('ClassificationState', () => {
  let store: Store;
  let categoriesServiceMock: Partial<ClassificationCategoriesService>;
  let classificationsServiceMock: Partial<ClassificationsService>;
  let domainServiceMock: Partial<DomainService>;

  beforeEach(async () => {
    categoriesServiceMock = {
      getClassificationCategoriesByType: vi.fn().mockReturnValue(of(mockClassificationTypes))
    };

    classificationsServiceMock = {
      getClassification: vi.fn().mockReturnValue(of(mockClassification)),
      getClassifications: vi.fn().mockReturnValue(of(mockClassificationList)),
      postClassification: vi.fn().mockReturnValue(of(mockClassification)),
      putClassification: vi.fn().mockReturnValue(of(mockClassification)),
      deleteClassification: vi.fn().mockReturnValue(of(null))
    };

    domainServiceMock = {
      getSelectedDomain: vi.fn().mockReturnValue(of('DOMAIN_A')),
      getSelectedDomainValue: vi.fn().mockReturnValue('DOMAIN_A'),
      domainChangedComplete: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([ClassificationState])],
      providers: [
        { provide: ClassificationCategoriesService, useValue: categoriesServiceMock },
        { provide: ClassificationsService, useValue: classificationsServiceMock },
        { provide: DomainService, useValue: domainServiceMock }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);

    store.reset({
      ...store.snapshot(),
      classification: { ...initialClassificationState }
    });
  });

  describe('SelectClassification', () => {
    it('should return of(null) when classificationId is undefined', async () => {
      const stateBefore = store.snapshot().classification;

      await store.dispatch(new SelectClassification(undefined)).toPromise();

      const stateAfter = store.snapshot().classification;
      // State should remain unchanged when id is undefined
      expect(stateAfter.selectedClassification).toEqual(stateBefore.selectedClassification);
    });

    it('should call getClassification service when classificationId is provided', async () => {
      await store.dispatch(new SelectClassification('CLI:001')).toPromise();

      expect(classificationsServiceMock.getClassification).toHaveBeenCalledWith('CLI:001');
    });

    it('should update selectedClassification in state when classificationId is provided', async () => {
      const fetchedClassification = { ...mockClassification, name: 'Fetched Classification' };
      (classificationsServiceMock.getClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(fetchedClassification)
      );

      await store.dispatch(new SelectClassification('CLI:001')).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toEqual(fetchedClassification);
    });

    it('should update selectedClassificationType from the fetched classification', async () => {
      const fetchedClassification = { ...mockClassification, type: 'DOCUMENT' };
      (classificationsServiceMock.getClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(fetchedClassification)
      );

      await store.dispatch(new SelectClassification('CLI:001')).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassificationType).toBe('DOCUMENT');
    });
  });

  describe('SetSelectedClassificationType', () => {
    it('should update selectedClassificationType when type exists in classificationTypes', async () => {
      await store.dispatch(new SetSelectedClassificationType('DOCUMENT')).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassificationType).toBe('DOCUMENT');
    });

    it('should clear selectedClassification when type is changed', async () => {
      await store.dispatch(new SetSelectedClassificationType('DOCUMENT')).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toBeUndefined();
    });

    it('should not update state when type does not exist in classificationTypes', async () => {
      const stateBefore = store.snapshot().classification;

      await store.dispatch(new SetSelectedClassificationType('INVALID_TYPE')).toPromise();

      const stateAfter = store.snapshot().classification;
      expect(stateAfter.selectedClassificationType).toBe(stateBefore.selectedClassificationType);
    });
  });

  describe('DeselectClassification', () => {
    it('should clear selectedClassification', async () => {
      await store.dispatch(new DeselectClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toBeUndefined();
    });

    it('should not affect other state properties', async () => {
      await store.dispatch(new DeselectClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassificationType).toBe('TASK');
      expect(state.classifications).toEqual(mockClassificationList.classifications);
    });
  });

  describe('CopyClassification', () => {
    it('should clear classificationId on copied classification', async () => {
      await store.dispatch(new CopyClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.classificationId).toBeUndefined();
    });

    it('should clear key on copied classification', async () => {
      await store.dispatch(new CopyClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.key).toBeNull();
    });

    it('should preserve other properties from the original classification', async () => {
      await store.dispatch(new CopyClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.name).toBe(mockClassification.name);
      expect(state.selectedClassification.type).toBe(mockClassification.type);
      expect(state.selectedClassification.domain).toBe(mockClassification.domain);
    });

    it('should set badgeMessage with classification name', async () => {
      await store.dispatch(new CopyClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.badgeMessage).toContain(mockClassification.name);
    });
  });

  describe('CreateClassification', () => {
    it('should set selectedClassification as a new empty classification', async () => {
      await store.dispatch(new CreateClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toBeDefined();
      expect(state.selectedClassification.classificationId).toBeUndefined();
    });

    it('should set domain from DomainService on new classification', async () => {
      await store.dispatch(new CreateClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.domain).toBe('DOMAIN_A');
    });

    it('should set the type from selectedClassificationType', async () => {
      await store.dispatch(new CreateClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.type).toBe('TASK');
    });

    it('should set badgeMessage to creating new classification', async () => {
      await store.dispatch(new CreateClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.badgeMessage).toContain('Creating new classification');
    });

    it('should set parentId when a classification is currently selected', async () => {
      // mockClassification has classificationId = 'CLI:001'
      await store.dispatch(new CreateClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.parentId).toBe(mockClassification.classificationId);
      expect(state.selectedClassification.parentKey).toBe(mockClassification.key);
    });
  });

  describe('RemoveSelectedClassification', () => {
    it('should call deleteClassification service with selected classification id', async () => {
      await store.dispatch(new RemoveSelectedClassification()).toPromise();

      expect(classificationsServiceMock.deleteClassification).toHaveBeenCalledWith(mockClassification.classificationId);
    });

    it('should clear selectedClassification from state after deletion', async () => {
      await store.dispatch(new RemoveSelectedClassification()).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toBeUndefined();
    });

    it('should remove the deleted classification from the classifications list', async () => {
      store.reset({
        ...store.snapshot(),
        classification: {
          ...initialClassificationState,
          classifications: [
            { classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Classification 1' },
            { classificationId: 'CLI:002', key: 'TEST_KEY_2', name: 'Classification 2' }
          ],
          selectedClassification: { ...mockClassification, classificationId: 'CLI:001' }
        }
      });

      await store.dispatch(new RemoveSelectedClassification()).toPromise();

      const state = store.snapshot().classification;
      const remainingIds = state.classifications.map((c) => c.classificationId);
      expect(remainingIds).not.toContain('CLI:001');
      expect(remainingIds).toContain('CLI:002');
    });
  });

  describe('InitializeStore (ngxsAfterBootstrap)', () => {
    it('should have classificationTypes defined after store reset', () => {
      const state = store.snapshot().classification;
      // After the state is reset in beforeEach, classificationTypes should be from our initialClassificationState
      expect(state.classificationTypes).toEqual(mockClassificationTypes);
    });

    it('should have a selectedClassificationType defined after store reset', () => {
      const state = store.snapshot().classification;
      expect(state.selectedClassificationType).toBe('TASK');
    });

    it('should have classificationTypes matching the mock after state reset', () => {
      // After store.reset() in beforeEach, the state should have classificationTypes from initialClassificationState
      const state = store.snapshot().classification;
      expect(state.classificationTypes).toBeDefined();
      expect(state.classificationTypes).toEqual(mockClassificationTypes);
    });
  });

  describe('GetClassifications', () => {
    it('should call getSelectedDomain from DomainService', async () => {
      await store.dispatch(new GetClassifications()).toPromise();

      expect(domainServiceMock.getSelectedDomain).toHaveBeenCalled();
    });

    it('should call getClassifications service with correct filter and sort parameters', async () => {
      await store.dispatch(new GetClassifications()).toPromise();

      expect(classificationsServiceMock.getClassifications).toHaveBeenCalledWith(
        { domain: ['DOMAIN_A'], type: ['TASK'] },
        { 'sort-by': 'KEY', order: 'ASCENDING' }
      );
    });

    it('should update classifications in state with the returned list', async () => {
      await store.dispatch(new GetClassifications()).toPromise();

      const state = store.snapshot().classification;
      expect(state.classifications).toEqual(mockClassificationList.classifications);
    });

    it('should call domainChangedComplete after fetching classifications', async () => {
      await store.dispatch(new GetClassifications()).toPromise();

      expect(domainServiceMock.domainChangedComplete).toHaveBeenCalled();
    });

    it('should use selectedClassificationType from state when building the filter', async () => {
      store.reset({
        ...store.snapshot(),
        classification: { ...initialClassificationState, selectedClassificationType: 'DOCUMENT' }
      });

      await store.dispatch(new GetClassifications()).toPromise();

      expect(classificationsServiceMock.getClassifications).toHaveBeenCalledWith(
        { domain: ['DOMAIN_A'], type: ['DOCUMENT'] },
        expect.any(Object)
      );
    });
  });

  describe('SaveCreatedClassification', () => {
    it('should call postClassification service with the given classification', async () => {
      await store.dispatch(new SaveCreatedClassification(mockClassification as any)).toPromise();

      expect(classificationsServiceMock.postClassification).toHaveBeenCalledWith(mockClassification);
    });

    it('should add the returned classification to the classifications list in state', async () => {
      const newClassification = { ...mockClassification, classificationId: 'CLI:NEW', name: 'New Classification' };
      (classificationsServiceMock.postClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(newClassification)
      );

      store.reset({
        ...store.snapshot(),
        classification: {
          ...initialClassificationState,
          classifications: [
            { classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Classification 1', type: 'TASK' },
            { classificationId: 'CLI:002', key: 'TEST_KEY_2', name: 'Classification 2', type: 'TASK' }
          ]
        }
      });

      await store.dispatch(new SaveCreatedClassification(newClassification as any)).toPromise();

      const state = store.snapshot().classification;
      const ids = state.classifications.map((c) => c.classificationId);
      expect(ids).toContain('CLI:NEW');
      expect(ids).toContain('CLI:001');
      expect(ids).toContain('CLI:002');
    });

    it('should set the returned classification as selectedClassification in state', async () => {
      const savedClassification = { ...mockClassification, name: 'Saved New Classification' };
      (classificationsServiceMock.postClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(savedClassification)
      );

      await store.dispatch(new SaveCreatedClassification(mockClassification as any)).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toEqual(savedClassification);
    });
  });

  describe('SaveModifiedClassification', () => {
    it('should call putClassification service with the given classification', async () => {
      await store.dispatch(new SaveModifiedClassification(mockClassification as any)).toPromise();

      expect(classificationsServiceMock.putClassification).toHaveBeenCalledWith(mockClassification);
    });

    it('should update the existing classification in the list via updateClassificationList', async () => {
      const updatedClassification = { ...mockClassification, classificationId: 'CLI:001', name: 'Updated Name' };
      (classificationsServiceMock.putClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(updatedClassification)
      );

      store.reset({
        ...store.snapshot(),
        classification: {
          ...initialClassificationState,
          classifications: [
            { classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Original Name', type: 'TASK' },
            { classificationId: 'CLI:002', key: 'TEST_KEY_2', name: 'Classification 2', type: 'TASK' }
          ]
        }
      });

      await store.dispatch(new SaveModifiedClassification(updatedClassification as any)).toPromise();

      const state = store.snapshot().classification;
      const updated = state.classifications.find((c) => c.classificationId === 'CLI:001');
      expect(updated.name).toBe('Updated Name');
    });

    it('should set the returned classification as selectedClassification', async () => {
      const savedClassification = { ...mockClassification, name: 'Modified Classification' };
      (classificationsServiceMock.putClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(savedClassification)
      );

      await store.dispatch(new SaveModifiedClassification(mockClassification as any)).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toEqual(savedClassification);
    });

    it('should leave other classifications in the list unchanged', async () => {
      const updatedClassification = { ...mockClassification, classificationId: 'CLI:001', name: 'Updated Name' };
      (classificationsServiceMock.putClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(updatedClassification)
      );

      store.reset({
        ...store.snapshot(),
        classification: {
          ...initialClassificationState,
          classifications: [
            { classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Original Name', type: 'TASK' },
            { classificationId: 'CLI:002', key: 'TEST_KEY_2', name: 'Classification 2', type: 'TASK' }
          ]
        }
      });

      await store.dispatch(new SaveModifiedClassification(updatedClassification as any)).toPromise();

      const state = store.snapshot().classification;
      const other = state.classifications.find((c) => c.classificationId === 'CLI:002');
      expect(other.name).toBe('Classification 2');
    });
  });

  describe('RestoreSelectedClassification', () => {
    it('should call getClassification when the classification exists in the list', async () => {
      // 'CLI:001' is in mockClassificationList.classifications
      await store.dispatch(new RestoreSelectedClassification('CLI:001')).toPromise();

      expect(classificationsServiceMock.getClassification).toHaveBeenCalledWith('CLI:001');
    });

    it('should update selectedClassification from the fetched result when classification exists', async () => {
      const restored = { ...mockClassification, name: 'Restored Classification' };
      (classificationsServiceMock.getClassification as ReturnType<typeof vi.fn>).mockReturnValue(of(restored));

      await store.dispatch(new RestoreSelectedClassification('CLI:001')).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toEqual(restored);
    });

    it('should restore to a new classification shell when classificationId is not in the list', async () => {
      // 'CLI:UNKNOWN' is not in the classifications list → takes the else-branch
      await store.dispatch(new RestoreSelectedClassification('CLI:UNKNOWN')).toPromise();

      const state = store.snapshot().classification;
      // selectedClassification should be a minimal shell with no classificationId or key
      expect(state.selectedClassification.classificationId).toBeUndefined();
      expect(state.selectedClassification.key).toBeUndefined();
    });

    it('should use the first category of the selected type when restoring to new shell', async () => {
      // selectedClassificationType is 'TASK', first category is 'EXTERNAL'
      await store.dispatch(new RestoreSelectedClassification('CLI:UNKNOWN')).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.category).toBe('EXTERNAL');
    });

    it('should carry over type, domain, parentId and parentKey from previous selectedClassification when restoring shell', async () => {
      await store.dispatch(new RestoreSelectedClassification('CLI:UNKNOWN')).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.type).toBe(mockClassification.type);
      expect(state.selectedClassification.domain).toBe(mockClassification.domain);
      expect(state.selectedClassification.parentId).toBe(mockClassification.parentId);
      expect(state.selectedClassification.parentKey).toBe(mockClassification.parentKey);
    });
  });

  describe('InitializeStore (dispatch by type)', () => {
    it('should call getClassificationCategoriesByType when InitializeStore type is dispatched', async () => {
      (categoriesServiceMock.getClassificationCategoriesByType as ReturnType<typeof vi.fn>).mockClear();
      await store.dispatch({ type: '[ClassificationState] Initializing state' }).toPromise();
      expect(categoriesServiceMock.getClassificationCategoriesByType).toHaveBeenCalled();
    });

    it('should update classificationTypes in state when InitializeStore is dispatched', async () => {
      await store.dispatch({ type: '[ClassificationState] Initializing state' }).toPromise();
      const state = store.snapshot().classification;
      expect(state.classificationTypes).toEqual(mockClassificationTypes);
    });
  });

  describe('ngxsAfterBootstrap', () => {
    it('should dispatch InitializeStore when ngxsAfterBootstrap is called', () => {
      const classificationState = TestBed.inject(ClassificationState);
      const mockCtx = { dispatch: vi.fn() };
      classificationState.ngxsAfterBootstrap(mockCtx as any);
      expect(mockCtx.dispatch).toHaveBeenCalled();
    });
  });

  describe('UpdateClassification', () => {
    it('should call putClassification service with the given classification', async () => {
      await store.dispatch(new UpdateClassification(mockClassification as any)).toPromise();

      expect(classificationsServiceMock.putClassification).toHaveBeenCalledWith(mockClassification);
    });

    it('should update the matching entry in the classifications list via updateClassificationList', async () => {
      const updatedClassification = { ...mockClassification, classificationId: 'CLI:001', name: 'Tree Updated Name' };
      (classificationsServiceMock.putClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(updatedClassification)
      );

      store.reset({
        ...store.snapshot(),
        classification: {
          ...initialClassificationState,
          classifications: [
            { classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Original Name', type: 'TASK' },
            { classificationId: 'CLI:002', key: 'TEST_KEY_2', name: 'Classification 2', type: 'TASK' }
          ]
        }
      });

      await store.dispatch(new UpdateClassification(updatedClassification as any)).toPromise();

      const state = store.snapshot().classification;
      const updated = state.classifications.find((c) => c.classificationId === 'CLI:001');
      expect(updated.name).toBe('Tree Updated Name');
    });

    it('should update selectedClassification when its id matches the returned classification', async () => {
      const updatedClassification = { ...mockClassification, classificationId: 'CLI:001', name: 'Tree Updated' };
      (classificationsServiceMock.putClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(updatedClassification)
      );

      store.reset({
        ...store.snapshot(),
        classification: {
          ...initialClassificationState,
          selectedClassification: { ...mockClassification, classificationId: 'CLI:001' },
          classifications: [{ classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Original', type: 'TASK' }]
        }
      });

      await store.dispatch(new UpdateClassification(updatedClassification as any)).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification).toEqual(updatedClassification);
    });

    it('should NOT change selectedClassification when its id does not match the returned classification', async () => {
      const updatedClassification = { ...mockClassification, classificationId: 'CLI:002', name: 'Other Updated' };
      (classificationsServiceMock.putClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(updatedClassification)
      );

      const differentSelected = { ...mockClassification, classificationId: 'CLI:001', name: 'Selected' };
      store.reset({
        ...store.snapshot(),
        classification: {
          ...initialClassificationState,
          selectedClassification: differentSelected,
          classifications: [
            { classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Selected', type: 'TASK' },
            { classificationId: 'CLI:002', key: 'TEST_KEY_2', name: 'Other', type: 'TASK' }
          ]
        }
      });

      await store.dispatch(new UpdateClassification(updatedClassification as any)).toPromise();

      const state = store.snapshot().classification;
      expect(state.selectedClassification.classificationId).toBe('CLI:001');
      expect(state.selectedClassification.name).toBe('Selected');
    });

    it('should leave other classifications unchanged in the list', async () => {
      const updatedClassification = { ...mockClassification, classificationId: 'CLI:001', name: 'Updated' };
      (classificationsServiceMock.putClassification as ReturnType<typeof vi.fn>).mockReturnValue(
        of(updatedClassification)
      );

      store.reset({
        ...store.snapshot(),
        classification: {
          ...initialClassificationState,
          classifications: [
            { classificationId: 'CLI:001', key: 'TEST_KEY_1', name: 'Original', type: 'TASK' },
            { classificationId: 'CLI:002', key: 'TEST_KEY_2', name: 'Unchanged', type: 'TASK' }
          ]
        }
      });

      await store.dispatch(new UpdateClassification(updatedClassification as any)).toPromise();

      const state = store.snapshot().classification;
      const unchanged = state.classifications.find((c) => c.classificationId === 'CLI:002');
      expect(unchanged.name).toBe('Unchanged');
    });
  });
});
