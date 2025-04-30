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

import { Action, NgxsOnInit, State, StateContext } from '@ngxs/store';
import { Observable, of } from 'rxjs';
import { CalculateNumberOfCards, SetFilterExpansion } from './workplace.actions';

@State<WorkplaceStateModel>({ name: 'WorkplaceState' })
export class WorkplaceState implements NgxsOnInit {
  @Action(SetFilterExpansion)
  setFilterExpansion(ctx: StateContext<WorkplaceStateModel>, action: SetFilterExpansion): Observable<null> {
    const param = action.isExpanded;
    const isExpanded = typeof param !== 'undefined' ? param : !ctx.getState().isFilterExpanded;

    ctx.setState({
      ...ctx.getState(),
      isFilterExpanded: isExpanded
    });

    ctx.dispatch(new CalculateNumberOfCards());

    return of(null);
  }

  @Action(CalculateNumberOfCards)
  calculateNumberOfCards(ctx: StateContext<WorkplaceStateModel>): Observable<null> {
    const cardHeight = 90;
    const totalHeight = window.innerHeight;
    const toolbarHeight = ctx.getState().isFilterExpanded ? 308 : 192;
    const occupiedHeight = 56 + 90 + toolbarHeight;

    const cards = Math.max(1, Math.round((totalHeight - occupiedHeight) / cardHeight));

    ctx.setState({
      ...ctx.getState(),
      cards: cards
    });

    return of(null);
  }

  ngxsOnInit(ctx: StateContext<WorkplaceStateModel>): void {
    this.calculateNumberOfCards(ctx);

    ctx.setState({
      ...ctx.getState(),
      isFilterExpanded: false
    });
  }
}

export interface WorkplaceStateModel {
  isFilterExpanded: boolean;
  cards: number;
}
