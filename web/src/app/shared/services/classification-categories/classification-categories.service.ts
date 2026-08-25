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

import { HttpClient } from '@angular/common/http';

import { inject, Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Customisation } from '../../models/customisation';

const customisationUrl = 'environments/data-sources/kadai-customization.json';

export const missingIcon = 'assets/icons/categories/missing-icon.svg';
export const asteriskIcon = './assets/icons/asterisk.svg';

export interface CategoriesResponse {
  [key: string]: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ClassificationCategoriesService {
  private httpClient = inject(HttpClient);

  // TODO: convert to Map (maybe via ES6)
  getClassificationCategoriesByType(): Observable<CategoriesResponse> {
    return this.httpClient.get<CategoriesResponse>(`${environment.kadaiRestUrl}/v1/classifications-by-type`);
  }

  getCustomisation(): Observable<Customisation> {
    return this.httpClient.get<Customisation>(customisationUrl).pipe(
      map((customisation) => {
        Object.keys(customisation).forEach((lang) => {
          if (customisation[lang]?.classifications?.categories) {
            customisation[lang].classifications.categories.missing = missingIcon;
            customisation[lang].classifications.categories.all = asteriskIcon;
          } else {
            if (customisation[lang]?.classifications) {
              customisation[lang].classifications.categories = {
                missing: missingIcon,
                all: asteriskIcon
              };
            } else {
              customisation[lang].classifications = {
                categories: {
                  missing: missingIcon,
                  all: asteriskIcon
                }
              };
            }
          }
        });
        return customisation;
      })
    );
  }
}
