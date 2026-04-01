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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NoAccessComponent } from './no-access.component';
import { Router } from '@angular/router';
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('NoAccessComponent', () => {
  let component: NoAccessComponent;
  let fixture: ComponentFixture<NoAccessComponent>;
  let routerMock: { navigate: ReturnType<typeof vi.fn> };
  let kadaiEngineServiceMock: { hasRole: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    routerMock = { navigate: vi.fn() };
    kadaiEngineServiceMock = { hasRole: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [NoAccessComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideAngularSvgIcon(),
        { provide: Router, useValue: routerMock },
        { provide: KadaiEngineService, useValue: kadaiEngineServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NoAccessComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    kadaiEngineServiceMock.hasRole.mockReturnValue(false);
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should navigate to administration when user has BusinessAdminRole', () => {
    kadaiEngineServiceMock.hasRole.mockImplementation((roles: string[]) => {
      return roles.includes('ADMIN') || roles.includes('BUSINESS_ADMIN');
    });

    fixture.detectChanges();

    expect(routerMock.navigate).toHaveBeenCalledWith(['administration']);
  });

  it('should navigate to monitor when user has MonitorRole but not BusinessAdminRole', () => {
    kadaiEngineServiceMock.hasRole.mockImplementation((roles: string[]) => {
      return roles.includes('MONITOR') && !roles.includes('BUSINESS_ADMIN');
    });

    fixture.detectChanges();

    expect(routerMock.navigate).toHaveBeenCalledWith(['monitor']);
  });

  it('should navigate to workplace when user has UserRole but not BusinessAdmin or Monitor roles', () => {
    kadaiEngineServiceMock.hasRole.mockImplementation((roles: string[]) => {
      return roles.includes('USER') && !roles.includes('BUSINESS_ADMIN') && !roles.includes('MONITOR');
    });

    fixture.detectChanges();

    expect(routerMock.navigate).toHaveBeenCalledWith(['workplace']);
  });

  it('should set showNoAccess to true when user has no roles', () => {
    kadaiEngineServiceMock.hasRole.mockReturnValue(false);

    fixture.detectChanges();

    expect(component.showNoAccess).toBe(true);
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('should initialize showNoAccess to false before ngOnInit', () => {
    expect(component.showNoAccess).toBe(false);
  });
});
