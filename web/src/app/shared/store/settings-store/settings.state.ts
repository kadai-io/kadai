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

import { Action, NgxsAfterBootstrap, State, StateContext } from '@ngxs/store';
import { inject, Injectable } from '@angular/core';
import { RetrieveSettings, SetSettings } from './settings.actions';
import { Settings } from '../../../settings/models/settings';
import { SettingsService } from '../../../settings/services/settings-service';
import { take } from 'rxjs/operators';
import { NotificationService } from '../../services/notifications/notification.service';

@Injectable({
  providedIn: 'root'
})
@State<SettingsStateModel>({ name: 'settings' })
export class SettingsState implements NgxsAfterBootstrap {
  private settingsService = inject(SettingsService);
  private notificationService = inject(NotificationService);

  @Action(RetrieveSettings)
  initializeStore(ctx: StateContext<SettingsStateModel>) {
    return this.settingsService
      .getSettings()
      .pipe(take(1))
      .subscribe((settings) => {
        if (!settings.schema) {
          this.notificationService.showError('SETTINGS_NO_SCHEMA');
        } else {
          ctx.patchState({
            settings: settings
          });
        }
      });
  }

  ngxsAfterBootstrap(ctx?: StateContext<any>): void {
    ctx.dispatch(new RetrieveSettings());
  }

  @Action(SetSettings)
  setSettings(ctx: StateContext<SettingsStateModel>, action: SetSettings) {
    return this.settingsService
      .updateSettings(action.settings)
      .pipe(take(1))
      .subscribe(() => {
        ctx.patchState({
          settings: action.settings
        });
      });
  }
}

export interface SettingsStateModel {
  settings: Settings;
}
