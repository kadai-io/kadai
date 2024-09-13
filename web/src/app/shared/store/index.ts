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

import { EngineConfigurationState } from './engine-configuration-store/engine-configuration.state';
import { ClassificationState } from './classification-store/classification.state';
import { WorkbasketState } from './workbasket-store/workbasket.state';
import { AccessItemsManagementState } from './access-items-management-store/access-items-management.state';
import { FilterState } from './filter-store/filter.state';
import { WorkplaceState } from './workplace-store/workplace.state';
import { SettingsState } from './settings-store/settings.state';

export const STATES = [
  EngineConfigurationState,
  ClassificationState,
  WorkbasketState,
  AccessItemsManagementState,
  FilterState,
  WorkplaceState,
  SettingsState
];
