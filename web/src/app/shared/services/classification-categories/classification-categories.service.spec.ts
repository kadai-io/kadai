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
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Customisation, CustomisationContent } from 'app/shared/models/customisation';
import {
  asteriskIcon,
  CategoriesResponse,
  ClassificationCategoriesService,
  missingIcon
} from './classification-categories.service';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from 'environments/environment';

describe('ClassificationCategoriesService', () => {
  let categoryService: ClassificationCategoriesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClassificationCategoriesService, provideHttpClientTesting()]
    });

    categoryService = TestBed.inject(ClassificationCategoriesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getClassificationCategoriesByType', () => {
    it('should fetch classifications by type and convert response object to Map', () => {
      const mockResponse: CategoriesResponse = {
        TASK: ['MANUAL', 'AUTOMATIC'],
        DOCUMENT: ['IMPORT', 'EXPORT']
      };

      categoryService.getClassificationCategoriesByType().subscribe((categoriesMap) => {
        expect(categoriesMap).toBeInstanceOf(Map);
        expect(categoriesMap.get('TASK')).toEqual(['MANUAL', 'AUTOMATIC']);
        expect(categoriesMap.get('DOCUMENT')).toEqual(['IMPORT', 'EXPORT']);
        expect(categoriesMap.size).toBe(2);
      });

      const req = httpMock.expectOne(`${environment.kadaiRestUrl}/v1/classifications-by-type`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('getCustomisation', () => {
    it('should insert missing and asterisk icons into customisation when categories exist', () => {
      const initialCustomisation: Customisation = {
        EN: { classifications: { categories: { EXISTING: 'custom-icon.svg' } } }
      };

      categoryService.getCustomisation().subscribe((customisation) => {
        expect(customisation.EN!.classifications!.categories!.missing).toBe(missingIcon);
        expect(customisation.EN!.classifications!.categories!.all).toBe(asteriskIcon);
        expect(customisation.EN!.classifications!.categories!['EXISTING']).toBe('custom-icon.svg');
      });

      const req = httpMock.expectOne('environments/data-sources/kadai-customization.json');
      expect(req.request.method).toBe('GET');
      req.flush(initialCustomisation);
    });

    it('should handle customisation objects without categories or without classifications', () => {
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
      });
    });
  });
});
