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

import { TestBed } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import { SettingsState } from './settings.state';
import { RetrieveSettings, SetSettings } from './settings.actions';
import { SettingsService } from '../../../settings/services/settings-service';
import { NotificationService } from '../../services/notifications/notification.service';
import { Settings } from '../../../settings/models/settings';

describe('SettingsState', () => {
  let store: Store;
  let settingsServiceMock: { getSettings: ReturnType<typeof vi.fn>; updateSettings: ReturnType<typeof vi.fn> };
  let notificationServiceMock: { showError: ReturnType<typeof vi.fn>; showSuccess: ReturnType<typeof vi.fn> };

  const mockSettings: Settings = {
    schema: [{ displayName: 'Test Group', members: [] }]
  };

  beforeEach(async () => {
    settingsServiceMock = {
      getSettings: vi.fn().mockReturnValue(of(mockSettings)),
      updateSettings: vi.fn().mockReturnValue(of(null))
    };

    notificationServiceMock = {
      showError: vi.fn(),
      showSuccess: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([SettingsState])],
      providers: [
        { provide: SettingsService, useValue: settingsServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock }
      ]
    }).compileComponents();

    store = TestBed.inject(Store);
  });

  it('should initialize the state', () => {
    const state = store.snapshot().settings;
    expect(state).toBeDefined();
  });

  it('should call patchState with settings when RetrieveSettings is dispatched and settings has schema', async () => {
    settingsServiceMock.getSettings.mockReturnValue(of(mockSettings));

    await store.dispatch(new RetrieveSettings()).toPromise();

    const state = store.snapshot().settings;
    expect(state.settings).toEqual(mockSettings);
  });

  it('should call notificationService.showError when settings has no schema', async () => {
    const settingsWithoutSchema = {} as Settings;
    settingsServiceMock.getSettings.mockReturnValue(of(settingsWithoutSchema));

    await store.dispatch(new RetrieveSettings()).toPromise();

    expect(notificationServiceMock.showError).toHaveBeenCalledWith('SETTINGS_NO_SCHEMA');
  });

  it('should not update state when settings has no schema', async () => {
    const settingsWithoutSchema = {} as Settings;
    settingsServiceMock.getSettings.mockReturnValue(of(settingsWithoutSchema));

    await store.dispatch(new RetrieveSettings()).toPromise();

    const state = store.snapshot().settings;
    // State should not have been updated with invalid settings
    expect(state.settings).not.toEqual(settingsWithoutSchema);
  });

  it('should update state when SetSettings is dispatched', async () => {
    const newSettings: Settings = {
      schema: [{ displayName: 'Updated Group', members: [] }]
    };
    settingsServiceMock.updateSettings.mockReturnValue(of(null));

    await store.dispatch(new SetSettings(newSettings)).toPromise();

    const state = store.snapshot().settings;
    expect(state.settings).toEqual(newSettings);
  });

  it('should call settingsService.updateSettings when SetSettings is dispatched', async () => {
    const newSettings: Settings = {
      schema: [{ displayName: 'Updated Group', members: [] }]
    };
    settingsServiceMock.updateSettings.mockReturnValue(of(null));

    await store.dispatch(new SetSettings(newSettings)).toPromise();

    expect(settingsServiceMock.updateSettings).toHaveBeenCalledWith(newSettings);
  });
});
