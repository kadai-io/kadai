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
import { of } from 'rxjs';
import { EngineConfigurationState } from './engine-configuration.state';
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
