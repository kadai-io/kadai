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
import { provideRouter, Router } from '@angular/router';
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';
import { SidenavService } from '../../services/sidenav/sidenav.service';
import { RequestInProgressService } from '../../services/request-in-progress/request-in-progress.service';
import { of } from 'rxjs';
import { MonitorRoles } from '../../roles/monitor.roles';
import { UserRoles } from '../../roles/user.roles';
import { BusinessAdminRoles } from '../../roles/business-admin.roles';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('SidenavListComponent', () => {
  let component: SidenavListComponent;
  let fixture: ComponentFixture<SidenavListComponent>;
  let debugElement: DebugElement;
  let KadaiEngineServiceSpy: Partial<KadaiEngineService>;
  let SidenavServiceSpy: Partial<SidenavService>;
  let RequestInProgressServiceSpy: Partial<RequestInProgressService>;

  beforeEach(async () => {
    KadaiEngineServiceSpy = {
      hasRole: vi.fn().mockReturnValue(false),
      isHistoryProviderEnabled: vi.fn().mockReturnValue(of(false)),
      isCustomRoutingRulesEnabled: vi.fn().mockReturnValue(of(false))
    };

    SidenavServiceSpy = {
      toggleSidenav: vi.fn()
    };

    RequestInProgressServiceSpy = {
      setRequestInProgress: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [SidenavListComponent],
      providers: [
        provideRouter([{ path: '**', children: [] }]),
        provideHttpClientTesting(),
        {
          provide: KadaiEngineService,
          useValue: KadaiEngineServiceSpy
        },
        {
          provide: SidenavService,
          useValue: SidenavServiceSpy
        },
        {
          provide: RequestInProgressService,
          useValue: RequestInProgressServiceSpy
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

  it('should show workplace links when user has workplace access only', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(UserRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const menuList = debugElement.queryAll(By.css('.navlist__item'));
    expect(menuList.length).toBe(3);
  });

  it('should show history link when history provider is enabled', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockReturnValue(false);
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(true));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const historyLink = debugElement.query(By.css('.navlist__history'));
    expect(historyLink).toBeTruthy();
  });

  it('should show settings link when user has admin access', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(BusinessAdminRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const settingsLink = debugElement.query(By.css('.navlist__setting'));
    expect(settingsLink).toBeTruthy();
  });

  it('should show routing link when custom routing is enabled', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockReturnValue(false);
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(true));
    fixture.detectChanges();
    const routingLink = debugElement.query(By.css('.navlist__admin-task-routing'));
    expect(routingLink).toBeTruthy();
  });

  it('should call sidenavService.toggleSidenav when monitor link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(MonitorRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const monitorLink = debugElement.query(By.css('.navlist__monitor')).nativeElement;
    monitorLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call sidenavService.toggleSidenav when workplace link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(UserRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const workplaceLink = debugElement.query(By.css('.navlist__workplace')).nativeElement;
    workplaceLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call sidenavService.toggleSidenav when history link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockReturnValue(false);
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(true));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const historyLink = debugElement.query(By.css('.navlist__history')).nativeElement;
    historyLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call sidenavService.toggleSidenav when settings link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(BusinessAdminRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const settingsLink = debugElement.query(By.css('.navlist__setting')).nativeElement;
    settingsLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should show no links when user has no access', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockReturnValue(false);
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const menuList = debugElement.queryAll(By.css('.navlist__item'));
    expect(menuList.length).toBe(0);
  });

  it('should call sidenavService.toggleSidenav when administration parent link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(BusinessAdminRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const adminLink = debugElement.query(By.css('.navlist__admin')).nativeElement;
    adminLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call sidenavService.toggleSidenav when classifications link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(BusinessAdminRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const classificationsLink = debugElement.query(By.css('.navlist__admin-classifications')).nativeElement;
    classificationsLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call sidenavService.toggleSidenav when access items link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(BusinessAdminRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const accessItemsLink = debugElement.query(By.css('.navlist__admin-access-items')).nativeElement;
    accessItemsLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call sidenavService.toggleSidenav when task routing link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockReturnValue(false);
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(true));
    fixture.detectChanges();
    const routingLink = debugElement.query(By.css('.navlist__admin-task-routing')).nativeElement;
    routingLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call sidenavService.toggleSidenav when workplace workbaskets sub-link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(UserRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const workplaceWorkbasketsLink = debugElement.query(By.css('.navlist__workplace--workbaskets')).nativeElement;
    workplaceWorkbasketsLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call sidenavService.toggleSidenav when workplace task-search sub-link is clicked', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(UserRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const taskSearchLink = debugElement.query(By.css('.navlist__workplace--task-search')).nativeElement;
    taskSearchLink.click();
    expect(SidenavServiceSpy.toggleSidenav).toHaveBeenCalled();
  });

  it('should call setRequestInProgress when navigating to a different route', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(MonitorRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const monitorLink = debugElement.query(By.css('.navlist__monitor')).nativeElement;
    monitorLink.click();
    expect(RequestInProgressServiceSpy.setRequestInProgress).toHaveBeenCalledWith(true);
  });

  it('should not call setRequestInProgress when current URL already contains the target route', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(MonitorRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'url', 'get').mockReturnValue('/kadai/monitor');
    const monitorLink = debugElement.query(By.css('.navlist__monitor')).nativeElement;
    monitorLink.click();
    expect(RequestInProgressServiceSpy.setRequestInProgress).not.toHaveBeenCalled();
  });

  it('should toggle the toggle property on each click', () => {
    vi.mocked(KadaiEngineServiceSpy.hasRole).mockImplementation((roles) => {
      return JSON.stringify(roles) === JSON.stringify(Object.values(MonitorRoles));
    });
    vi.mocked(KadaiEngineServiceSpy.isHistoryProviderEnabled).mockReturnValue(of(false));
    vi.mocked(KadaiEngineServiceSpy.isCustomRoutingRulesEnabled).mockReturnValue(of(false));
    fixture.detectChanges();
    expect(component.toggle).toBe(false);
    const monitorLink = debugElement.query(By.css('.navlist__monitor')).nativeElement;
    monitorLink.click();
    expect(component.toggle).toBe(true);
    monitorLink.click();
    expect(component.toggle).toBe(false);
  });
});
