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

import {
  AccessItemsCustomisation,
  ClassificationCategoryImages,
  ClassificationsCustomisation,
  GlobalCustomisation,
  TasksCustomisation,
  WorkbasketsCustomisation
} from 'app/shared/models/customisation';
import { Selector } from '@ngxs/store';
import { EngineConfigurationState, EngineConfigurationStateModel } from './engine-configuration.state';

export class EngineConfigurationSelectors {
  @Selector([EngineConfigurationState])
  static globalCustomisation(state: EngineConfigurationStateModel): GlobalCustomisation {
    return state.customisation[state.language].global;
  }

  @Selector([EngineConfigurationState])
  static workbasketsCustomisation(state: EngineConfigurationStateModel): WorkbasketsCustomisation {
    return state.customisation[state.language].workbaskets;
  }

  @Selector([EngineConfigurationState])
  static classificationsCustomisation(state: EngineConfigurationStateModel): ClassificationsCustomisation {
    return state.customisation[state.language].classifications;
  }

  @Selector([EngineConfigurationState])
  static accessItemsCustomisation(state: EngineConfigurationStateModel): AccessItemsCustomisation {
    return state.customisation[state.language].workbaskets['access-items'];
  }

  @Selector([EngineConfigurationState])
  static tasksCustomisation(state: EngineConfigurationStateModel): TasksCustomisation {
    return state.customisation[state.language].tasks;
  }

  @Selector([EngineConfigurationState])
  static selectCategoryIcons(state: EngineConfigurationStateModel): ClassificationCategoryImages {
    return {
      ...state.customisation[state.language].classifications.categories
    };
  }
}
