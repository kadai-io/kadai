/*
 * Copyright [2024] [envite consulting GmbH]
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

import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { Store, Select } from '@ngxs/store';
import { ClassificationSelectors } from 'app/shared/store/classification-store/classification.selectors';
import { SetSelectedClassificationType } from 'app/shared/store/classification-store/classification.actions';
import { Location } from '@angular/common';

@Component({
  selector: 'kadai-administration-classification-types-selector',
  templateUrl: './classification-types-selector.component.html',
  styleUrls: ['./classification-types-selector.component.scss']
})
export class ClassificationTypesSelectorComponent {
  @Select(ClassificationSelectors.selectedClassificationType) classificationTypeSelected$: Observable<string>;
  @Select(ClassificationSelectors.classificationTypes) classificationTypes$: Observable<string[]>;

  constructor(
    private store: Store,
    private location: Location
  ) {}

  select(value: string): void {
    this.store.dispatch(new SetSelectedClassificationType(value));
    this.location.go(this.location.path().replace(/(classifications).*/g, 'classifications'));
  }
}
