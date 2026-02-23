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
import { provideRouter, NavigationEnd } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { SelectedRouteService } from './selected-route';

describe('SelectedRouteService', () => {
  let service: SelectedRouteService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SelectedRouteService, provideRouter([])]
    });
    service = TestBed.inject(SelectedRouteService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getSelectedRoute', () => {
    it('should return an observable', () => {
      const obs = service.getSelectedRoute();
      expect(obs).toBeDefined();
      expect(typeof obs.subscribe).toBe('function');
    });
  });

  describe('selectRoute', () => {
    it('should emit "workplace" when URL contains workplace', () => {
      const event = new NavigationEnd(1, '/kadai/workplace', '/kadai/workplace');
      let emitted: string;
      service.getSelectedRoute().subscribe((route) => (emitted = route));

      service.selectRoute(event);

      expect(emitted).toBe('workplace');
    });

    it('should emit "administration" when URL contains administration', () => {
      const event = new NavigationEnd(2, '/kadai/administration/workbaskets', '/kadai/administration/workbaskets');
      let emitted: string;
      service.getSelectedRoute().subscribe((route) => (emitted = route));

      service.selectRoute(event);

      expect(emitted).toBe('administration');
    });

    it('should emit "monitor" when URL contains monitor', () => {
      const event = new NavigationEnd(3, '/kadai/monitor', '/kadai/monitor');
      let emitted: string;
      service.getSelectedRoute().subscribe((route) => (emitted = route));

      service.selectRoute(event);

      expect(emitted).toBe('monitor');
    });

    it('should emit "history" when URL contains history', () => {
      const event = new NavigationEnd(4, '/kadai/history', '/kadai/history');
      let emitted: string;
      service.getSelectedRoute().subscribe((route) => (emitted = route));

      service.selectRoute(event);

      expect(emitted).toBe('history');
    });

    it('should emit "settings" when URL contains settings', () => {
      const event = new NavigationEnd(5, '/kadai/settings', '/kadai/settings');
      let emitted: string;
      service.getSelectedRoute().subscribe((route) => (emitted = route));

      service.selectRoute(event);

      expect(emitted).toBe('settings');
    });

    it('should emit empty string when URL does not match any known route', () => {
      const event = new NavigationEnd(6, '/unknown/path', '/unknown/path');
      let emitted: string;
      service.getSelectedRoute().subscribe((route) => (emitted = route));

      service.selectRoute(event);

      expect(emitted).toBe('');
    });

    it('should emit multiple values as selectRoute is called multiple times', () => {
      const emittedValues: string[] = [];
      service.getSelectedRoute().subscribe((route) => emittedValues.push(route));

      service.selectRoute(new NavigationEnd(1, '/kadai/workplace', '/kadai/workplace'));
      service.selectRoute(new NavigationEnd(2, '/kadai/administration', '/kadai/administration'));
      service.selectRoute(new NavigationEnd(3, '/kadai/monitor', '/kadai/monitor'));

      expect(emittedValues).toEqual(['workplace', 'administration', 'monitor']);
    });

    it('should use urlAfterRedirects for route detection', () => {
      const event = new NavigationEnd(1, '/original-url', '/kadai/workplace');
      let emitted: string;
      service.getSelectedRoute().subscribe((route) => (emitted = route));

      service.selectRoute(event);

      expect(emitted).toBe('workplace');
    });
  });
});
