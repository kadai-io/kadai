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

import { Component, inject, OnInit, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { Observable } from 'rxjs';
import { Store } from '@ngxs/store';
import { Settings, SettingTypes } from 'app/settings/models/settings';
import { NotificationService } from 'app/shared/services/notifications/notification.service';
import { SetSettings } from 'app/shared/store/settings-store/settings.actions';
import { SettingsSelectors } from 'app/shared/store/settings-store/settings.selectors';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { intervalValidator, jsonValidator } from './settings.validators';

import { MatButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { SettingsMember } from 'app/settings/models/settings-member';

@Component({
  selector: 'kadai-administration-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
  imports: [
    MatButton,
    MatTooltip,
    MatIcon,
    MatFormField,
    MatLabel,
    MatInput,
    MatError,
    ReactiveFormsModule,
    CdkTextareaAutosize
  ]
})
export class SettingsComponent implements OnInit {
  settingTypes = SettingTypes;
  settings = signal<Settings | undefined>(undefined);
  settingsForm!: FormGroup;

  private rawInitialSettings?: Settings;
  private destroyRef = inject(DestroyRef);

  settings$: Observable<Settings> = inject(Store).select(SettingsSelectors.getSettings);
  private store = inject(Store);
  private fb = inject(FormBuilder);
  private notificationService = inject(NotificationService);
  private requestInProgressService = inject(RequestInProgressService);

  ngOnInit() {
    this.settings$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((settings) => {
      this.requestInProgressService.setRequestInProgress(false);
      if (settings) {
        this.rawInitialSettings = settings;
        this.settings.set(settings);
        this.buildForm(settings);
      }
    });
  }

  private buildForm(settings: Settings): void {
    const groupControls: Record<string, AbstractControl> = {};

    settings.schema?.forEach((group) => {
      group.members?.forEach((member) => {
        const control = this.createControlForMember(member, settings[member.key]);
        if (control) {
          groupControls[member.key] = control;
        }
      });
    });

    this.settingsForm = this.fb.group(groupControls);
  }

  private createControlForMember(member: SettingsMember, value: unknown): AbstractControl | null {
    switch (member.type) {
      case SettingTypes.Text:
        return this.createTextControl(member, value);

      case SettingTypes.Interval:
        return this.createIntervalGroup(member, value);

      case SettingTypes.Color:
        return this.fb.control(value ?? '#000000');

      case SettingTypes.Json:
        return this.fb.control(value ?? '', [jsonValidator()]);

      default:
        return null;
    }
  }

  private createTextControl(member: SettingsMember, value: unknown): FormControl {
    const validators: ValidatorFn[] = [];
    if (member.min !== undefined) {
      validators.push(Validators.minLength(member.min));

      if (member.min > 0) {
        validators.push(Validators.required);
      }
    }

    if (member.max !== undefined) validators.push(Validators.maxLength(member.max));

    return this.fb.control(value ?? '', validators);
  }

  private createIntervalGroup(member: SettingsMember, value: unknown): FormGroup {
    const [lower, upper] = Array.isArray(value) ? value : [0, 0];

    return this.fb.group(
      {
        lower: [lower],
        upper: [upper]
      },
      { validators: [intervalValidator(member.min, member.max)] }
    );
  }

  onSave() {
    if (!this.settingsForm) return;

    if (this.settingsForm.invalid) {
      this.settingsForm.markAllAsTouched();
      this.notificationService.showError('SETTINGS_SAVE');
      return;
    }

    const updatedValue = this.extractFormValues(this.settingsForm.value);
    const updatedSettings: Settings = {
      ...this.rawInitialSettings,
      ...updatedValue
    } as Settings;

    this.store.dispatch(new SetSettings(updatedSettings)).subscribe(() => {
      this.notificationService.showSuccess('SETTINGS_SAVE');
    });
  }

  onReset() {
    if (!this.rawInitialSettings || !this.settingsForm) return;

    const resetValues = this.mapSettingsToFormValues(this.rawInitialSettings);
    this.settingsForm.reset(resetValues);
  }

  private extractFormValues(formValue: Record<string, unknown>): Record<string, unknown> {
    const updatedValue: Record<string, unknown> = {};

    Object.keys(formValue).forEach((key) => {
      const val = formValue[key];
      if (val && typeof val === 'object' && 'lower' in val && 'upper' in val) {
        updatedValue[key] = [val.lower, val.upper];
      } else {
        updatedValue[key] = val;
      }
    });

    return updatedValue;
  }

  private mapSettingsToFormValues(settings: Settings): Record<string, unknown> {
    const resetValues: Record<string, unknown> = {};

    settings.schema?.forEach((group) => {
      group.members?.forEach((member) => {
        const rawVal = settings[member.key];
        if (member.type === SettingTypes.Interval && Array.isArray(rawVal)) {
          resetValues[member.key] = { lower: rawVal[0], upper: rawVal[1] };
        } else {
          resetValues[member.key] = rawVal;
        }
      });
    });

    return resetValues;
  }
}
