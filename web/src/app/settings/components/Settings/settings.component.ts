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

import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Settings, SettingTypes } from '../../models/settings';
import { Store } from '@ngxs/store';
import { NotificationService } from '../../../shared/services/notifications/notification.service';
import { SetSettings } from '../../../shared/store/settings-store/settings.actions';
import { SettingsSelectors } from '../../../shared/store/settings-store/settings.selectors';
import { takeUntil } from 'rxjs/operators';
import { validateSettings } from './settings.validators';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';

import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';

@Component({
  selector: 'kadai-administration-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
  imports: [MatButton, MatTooltip, MatIcon, MatFormField, MatLabel, MatInput, FormsModule, CdkTextareaAutosize]
})
export class SettingsComponent implements OnInit, OnDestroy {
  settingTypes = SettingTypes;
  settings: Settings;
  oldSettings: Settings;
  invalidMembers: string[] = [];
  destroy$ = new Subject<void>();
  settings$: Observable<Settings> = inject(Store).select(SettingsSelectors.getSettings);
  private store = inject(Store);
  private notificationService = inject(NotificationService);
  private requestInProgressService = inject(RequestInProgressService);

  ngOnInit() {
    this.settings$.pipe(takeUntil(this.destroy$)).subscribe((settings) => {
      this.requestInProgressService.setRequestInProgress(false);
      this.settings = this.deepCopy(settings);
      this.oldSettings = this.deepCopy(settings);
    });
  }

  deepCopy(settings: Settings): Settings {
    return JSON.parse(JSON.stringify(settings));
  }

  onSave() {
    this.changeLabelColor('grey');
    this.invalidMembers = validateSettings(this.settings);
    if (this.invalidMembers.length === 0) {
      this.store.dispatch(new SetSettings(this.settings)).subscribe(() => {
        this.notificationService.showSuccess('SETTINGS_SAVE');
      });
    } else {
      this.changeLabelColor('red');
      this.notificationService.showError('SETTINGS_SAVE');
    }
  }

  changeLabelColor(color: string) {
    this.invalidMembers.forEach((member) => {
      const elements = Array.from(document.getElementsByClassName(member));
      elements.forEach((element) => {
        (element as HTMLElement).style.color = color;
      });
    });
  }

  onReset() {
    this.changeLabelColor('grey');
    this.settings = this.deepCopy(this.oldSettings);
  }

  onColorChange(key: string) {
    this.settings[key] = (document.getElementById(key) as HTMLInputElement).value;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
