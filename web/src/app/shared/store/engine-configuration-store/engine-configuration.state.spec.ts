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
import { beforeEach, describe, expect, it } from 'vitest';
import { firstValueFrom, of } from 'rxjs';
import { EngineConfigurationState } from './engine-configuration.state';
import { EngineConfigurationSelectors } from './engine-configuration.selectors';
import { ClassificationCategoriesService } from '../../services/classification-categories/classification-categories.service';
import { Customisation } from '../../models/customisation';

describe('EngineConfigurationState', () => {
  let store: Store;

  const mockCustomisation: Customisation = {
    EN: {
      workbaskets: {},
      classifications: {},
      tasks: {}
    }
  };

  const classificationCategoriesServiceMock = {
    getCustomisation: () => of(mockCustomisation),
    getClassificationCategoriesByType: () => of({})
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([EngineConfigurationState])],
      providers: [
        {
          provide: ClassificationCategoriesService,
          useValue: classificationCategoriesServiceMock
        }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
  });

  it('should initialize the state', () => {
    const state = store.snapshot().engineConfiguration;
    expect(state).toBeDefined();
    expect(state.language).toBe('EN');
    expect(state.customisation).toEqual(mockCustomisation);
    expect(state.customisation['EN']).toBeDefined();
  });
});

describe('EngineConfigurationSelectors', () => {
  let store: Store;

  const classificationCategoriesServiceMock = {
    getCustomisation: () =>
      of({
        EN: {
          global: { debounceTimeLookupField: 50 },
          workbaskets: {
            information: { custom1: { field: 'Field 1', visible: true } },
            'access-items': { accessId: { lookupField: true } }
          },
          classifications: {
            information: { custom1: { field: 'Class 1', visible: true } },
            categories: {
              EXTERNAL: 'assets/icons/categories/external.svg',
              MANUAL: 'assets/icons/categories/manual.svg'
            }
          },
          tasks: { information: { owner: { lookupField: true } } }
        }
      }),
    getClassificationCategoriesByType: () => of({})
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([EngineConfigurationState])],
      providers: [
        {
          provide: ClassificationCategoriesService,
          useValue: classificationCategoriesServiceMock
        }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
  });

  it('should select global customisation', async () => {
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.globalCustomisation));
    expect(result).toEqual({ debounceTimeLookupField: 50 });
  });

  it('should select workbaskets customisation', async () => {
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.workbasketsCustomisation));
    expect(result).toBeDefined();
    expect(result?.information).toBeDefined();
  });

  it('should select classifications customisation', async () => {
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.classificationsCustomisation));
    expect(result).toBeDefined();
    expect(result?.information).toBeDefined();
  });

  it('should select access items customisation when workbaskets exists', async () => {
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.accessItemsCustomisation));
    expect(result).toBeDefined();
    expect(result?.accessId).toEqual({ lookupField: true });
  });

  it('should select tasks customisation', async () => {
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.tasksCustomisation));
    expect(result).toBeDefined();
    expect(result?.information).toBeDefined();
  });

  it('should select category icons when categories exist', async () => {
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.selectCategoryIcons));
    expect(result).toEqual({
      EXTERNAL: 'assets/icons/categories/external.svg',
      MANUAL: 'assets/icons/categories/manual.svg'
    });
  });

  it('should return empty object for category icons when classifications has no categories', async () => {
    store.reset({
      ...store.snapshot(),
      engineConfiguration: {
        language: 'EN',
        customisation: {
          EN: {
            workbaskets: {},
            classifications: {},
            tasks: {}
          }
        }
      }
    });
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.selectCategoryIcons));
    expect(result).toEqual({});
  });

  it('should return empty object for category icons when classifications is undefined', async () => {
    store.reset({
      ...store.snapshot(),
      engineConfiguration: {
        language: 'EN',
        customisation: {
          EN: {
            workbaskets: {},
            classifications: undefined as any,
            tasks: {}
          }
        }
      }
    });
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.selectCategoryIcons));
    expect(result).toEqual({});
  });

  it('should return undefined for access items customisation when workbaskets is undefined', async () => {
    store.reset({
      ...store.snapshot(),
      engineConfiguration: {
        language: 'EN',
        customisation: {
          EN: {
            workbaskets: undefined as any,
            classifications: {},
            tasks: {}
          }
        }
      }
    });
    const result = await firstValueFrom(store.select(EngineConfigurationSelectors.accessItemsCustomisation));
    expect(result).toBeUndefined();
  });
});
