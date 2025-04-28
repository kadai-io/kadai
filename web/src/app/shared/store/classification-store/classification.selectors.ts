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

import { Selector } from '@ngxs/store';
import { ClassificationState, ClassificationStateModel } from './classification.state';
import { Classification } from '../../models/classification';

export class ClassificationSelectors {
  @Selector([ClassificationState])
  static classificationTypes(state: ClassificationStateModel): string[] {
    return Object.keys(state.classificationTypes);
  }

  @Selector([ClassificationState])
  static selectedClassificationType(state: ClassificationStateModel): string {
    return state.selectedClassificationType;
  }

  @Selector([ClassificationState])
  static selectCategories(state: ClassificationStateModel): string[] {
    return state.classificationTypes[state.selectedClassificationType];
  }

  @Selector([ClassificationState])
  static classifications(state: ClassificationStateModel): Classification[] {
    return state.classifications;
  }

  @Selector([ClassificationState])
  static selectedClassification(state: ClassificationStateModel): Classification {
    return state.selectedClassification;
  }

  @Selector([ClassificationState])
  static selectedClassificationId(state: ClassificationStateModel): string {
    return state.selectedClassification?.classificationId;
  }

  @Selector([ClassificationState])
  static getBadgeMessage(state: ClassificationStateModel): string {
    return state.badgeMessage;
  }
}
