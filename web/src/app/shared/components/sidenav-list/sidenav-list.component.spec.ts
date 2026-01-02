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
import { DebugElement } from '@angular/core';
import { SidenavListComponent } from './sidenav-list.component';
import { By } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';
import { SidenavService } from '../../services/sidenav/sidenav.service';
import { of } from 'rxjs';
import { MonitorRoles } from '../../roles/monitor.roles';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('SidenavListComponent', () => {
  let component: SidenavListComponent;
  let fixture: ComponentFixture<SidenavListComponent>;
  let debugElement: DebugElement;
  let KadaiEngineServiceSpy: Partial<KadaiEngineService>;
  let SidenavServiceSpy: Partial<SidenavService>;

  beforeEach(async () => {
    KadaiEngineServiceSpy = {
      hasRole: vi.fn().mockReturnValue(false),
      isHistoryProviderEnabled: vi.fn().mockReturnValue(of(false)),
      isCustomRoutingRulesEnabled: vi.fn().mockReturnValue(of(false))
    };

    SidenavServiceSpy = {
      toggleSidenav: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [SidenavListComponent],
      providers: [
        provideRouter([]),
        provideHttpClientTesting(),
        {
          provide: KadaiEngineService,
          useValue: KadaiEngineServiceSpy
        },
        {
          provide: SidenavService,
          useValue: SidenavServiceSpy
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SidenavListComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should show all links if user has all permissions', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockReturnValue(true);
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(true));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(true));
    fixture.detectChanges();
    const menuList = debugElement.queryAll(By.css('.navlist__item'));
    expect(menuList.length).toBe(11);
  });

  it('should show all links if user has only monitor access', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(MonitorRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const menuList = debugElement.queryAll(By.css('.navlist__item'));
    expect(menuList.length).toBe(1);
  });

  it('should toggle sidenav when link clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockReturnValue(true);
    component.toggle = true;
    fixture.detectChanges();
    const button = debugElement.query(By.css('.navlist__admin-workbaskets')).nativeElement;
    expect(button).toBeTruthy();
    button.click();
    expect(component.toggle).toBe(false);
  });
});
