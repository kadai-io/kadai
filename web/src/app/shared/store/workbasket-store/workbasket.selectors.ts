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
import { WorkbasketState, WorkbasketStateModel } from './workbasket.state';
import { WorkbasketSummary } from '../../models/workbasket-summary';
import { WorkbasketSummaryRepresentation } from '../../models/workbasket-summary-representation';
import { ACTION } from '../../models/action';
import { WorkbasketAccessItemsRepresentation } from '../../models/workbasket-access-items-representation';
import { Workbasket } from '../../models/workbasket';
import { WorkbasketComponent } from '../../../administration/models/workbasket-component';
import { ButtonAction } from '../../../administration/models/button-action';

export class WorkbasketSelectors {
  // Workbasket
  @Selector([WorkbasketState])
  static selectedWorkbasket(state: WorkbasketStateModel): Workbasket {
    return { ...state.selectedWorkbasket };
  }

  @Selector([WorkbasketState])
  static workbasketsSummary(state: WorkbasketStateModel): WorkbasketSummary[] {
    return state.paginatedWorkbasketsSummary?.workbaskets;
  }

  @Selector([WorkbasketState])
  static workbasketsSummaryRepresentation(state: WorkbasketStateModel): WorkbasketSummaryRepresentation {
    return state.paginatedWorkbasketsSummary;
  }

  @Selector([WorkbasketState])
  static workbasketActiveAction(state: WorkbasketStateModel): ACTION {
    return state.action;
  }

  @Selector([WorkbasketState])
  static selectedWorkbasketAndAction(state: WorkbasketStateModel): WorkbasketAndAction {
    return {
      selectedWorkbasket: state.selectedWorkbasket,
      action: state.action
    };
  }

  @Selector([WorkbasketState])
  static selectedWorkbasketAndComponentAndAction(state: WorkbasketStateModel): WorkbasketAndComponentAndAction {
    return {
      selectedWorkbasket: state.selectedWorkbasket,
      action: state.action,
      selectedComponent: state.selectedComponent
    };
  }

  @Selector([WorkbasketState])
  static selectedComponent(state: WorkbasketStateModel): WorkbasketComponent {
    return state.selectedComponent;
  }

  @Selector([WorkbasketState])
  static buttonAction(state: WorkbasketStateModel): ButtonAction {
    return state?.button;
  }

  // Workbasket Access Items
  @Selector([WorkbasketState])
  static workbasketAccessItems(state: WorkbasketStateModel): WorkbasketAccessItemsRepresentation {
    return state.workbasketAccessItems;
  }

  // Workbasket Distribution Targets
  @Selector([WorkbasketState])
  static workbasketDistributionTargets(state: WorkbasketStateModel): WorkbasketSummary[] {
    return state.workbasketDistributionTargets.distributionTargets;
  }

  @Selector([WorkbasketState])
  static availableDistributionTargets(state: WorkbasketStateModel): WorkbasketSummary[] {
    return state.availableDistributionTargets.workbaskets;
  }

  @Selector([WorkbasketState])
  static badgeMessage(state: WorkbasketStateModel): string {
    return state.badgeMessage;
  }
}

export interface WorkbasketAndAction {
  selectedWorkbasket: Workbasket;
  action: ACTION;
}

export interface WorkbasketAndComponentAndAction {
  selectedWorkbasket: Workbasket;
  action: ACTION;
  selectedComponent: WorkbasketComponent;
}
