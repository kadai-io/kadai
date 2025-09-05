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

import { Customisation } from 'app/shared/models/customisation';
import { Action, NgxsOnInit, State, StateContext } from '@ngxs/store';
import { ClassificationCategoriesService } from 'app/shared/services/classification-categories/classification-categories.service';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { inject, Injectable } from '@angular/core';

class InitializeStore {
  static readonly type = '[EngineConfigurationState] Initializing state';
}

@Injectable({
  providedIn: 'root'
})
@State<EngineConfigurationStateModel>({ name: 'engineConfiguration' })
export class EngineConfigurationState implements NgxsOnInit {
  private categoryService = inject(ClassificationCategoriesService);

  @Action(InitializeStore)
  initializeStore(ctx: StateContext<EngineConfigurationStateModel>): Observable<any> {
    return this.categoryService.getCustomisation().pipe(
      tap((customisation) =>
        ctx.setState({
          ...ctx.getState(),
          customisation,
          language: 'EN'
        })
      )
    );
  }

  ngxsOnInit(ctx: StateContext<EngineConfigurationStateModel>): void {
    ctx.dispatch(new InitializeStore());
  }
}

export interface EngineConfigurationStateModel {
  customisation: Customisation;
  language: string;
}
