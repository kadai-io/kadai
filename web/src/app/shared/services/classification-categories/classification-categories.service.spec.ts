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

import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Customisation, CustomisationContent } from 'app/shared/models/customisation';
import { asteriskIcon, ClassificationCategoriesService, missingIcon } from './classification-categories.service';
import { provideHttpClient } from '@angular/common/http';

describe('ClassificationCategoriesService', () => {
  let categoryService: ClassificationCategoriesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClassificationCategoriesService, provideHttpClient(), provideHttpClientTesting()]
    });

    categoryService = TestBed.inject(ClassificationCategoriesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should insert missing icon into customisation', waitForAsync(() => {
    const expectedCustomisationContent: CustomisationContent = {
      classifications: { categories: { all: asteriskIcon, missing: missingIcon } }
    };

    const expectedCustomisation: Customisation = {
      EN: expectedCustomisationContent,
      DE: expectedCustomisationContent
    };

    const initialCustomisations: Customisation[] = [
      {
        EN: { classifications: { categories: {} } },
        DE: { classifications: { categories: {} } }
      },
      { EN: { classifications: {} }, DE: { classifications: {} } },
      { EN: {}, DE: {} }
    ];

    initialCustomisations.forEach((initialCustomisation) => {
      categoryService.getCustomisation().subscribe((customisation) => {
        expect(customisation).toEqual(expectedCustomisation);
      });

      httpMock.expectOne('environments/data-sources/kadai-customization.json').flush(initialCustomisation);

      httpMock.verify();
    });
  }));
});
