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

import { inject, TestBed } from '@angular/core/testing';

import { SidenavService } from './sidenav.service';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MatSidenav } from '@angular/material/sidenav';

describe('SidenavService', () => {
  let service: SidenavService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SidenavService]
    });
    service = TestBed.inject(SidenavService);
  });

  it('should be created', inject([SidenavService], (injectedService: SidenavService) => {
    expect(injectedService).toBeTruthy();
  }));

  describe('setSidenav', () => {
    it('should set the internal sidenav reference', () => {
      const mockSidenav = { toggle: vi.fn(), opened: true } as any as MatSidenav;

      service.setSidenav(mockSidenav);

      service.toggleSidenav();
      expect(mockSidenav.toggle).toHaveBeenCalled();
    });
  });

  describe('toggleSidenav', () => {
    it('should call toggle on the sidenav', () => {
      const mockSidenav = { toggle: vi.fn(), opened: false } as any as MatSidenav;
      service.setSidenav(mockSidenav);

      service.toggleSidenav();

      expect(mockSidenav.toggle).toHaveBeenCalledTimes(1);
    });

    it('should update the state to match sidenav.opened when sidenav is open', () => {
      const mockSidenav = { toggle: vi.fn(), opened: true } as any as MatSidenav;
      service.setSidenav(mockSidenav);

      service.toggleSidenav();

      expect(service.state).toBe(true);
    });

    it('should update the state to false when sidenav.opened is false', () => {
      const mockSidenav = { toggle: vi.fn(), opened: false } as any as MatSidenav;
      service.setSidenav(mockSidenav);

      service.toggleSidenav();

      expect(service.state).toBe(false);
    });

    it('should reflect changed opened value after toggle', () => {
      let openedValue = true;
      const mockSidenav = {
        toggle: vi.fn().mockImplementation(() => {
          openedValue = !openedValue;
        }),
        get opened() {
          return openedValue;
        }
      } as any as MatSidenav;

      service.setSidenav(mockSidenav);

      service.toggleSidenav();

      expect(mockSidenav.toggle).toHaveBeenCalled();
      expect(service.state).toBe(false);
    });

    it('should start with state false by default', () => {
      expect(service.state).toBe(false);
    });
  });
});
