/*
 * Copyright [2024] [envite consulting GmbH]
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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { SidenavListComponent } from './sidenav-list.component';
import { SidenavService } from '../../services/sidenav/sidenav.service';
import { BrowserModule, By } from '@angular/platform-browser';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { EMPTY } from 'rxjs';
import { RequestInProgressService } from '../../services/request-in-progress/request-in-progress.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

const SidenavServiceSpy: Partial<SidenavService> = {
  toggleSidenav: jest.fn().mockReturnValue(EMPTY)
};

const KadaiEngineServiceSpy: Partial<KadaiEngineService> = {
  hasRole: jest.fn().mockReturnValue(EMPTY),
  isHistoryProviderEnabled: jest.fn().mockReturnValue(EMPTY),
  isCustomRoutingRulesEnabled: jest.fn().mockReturnValue(EMPTY)
};

describe('SidenavListComponent', () => {
  let component: SidenavListComponent;
  let fixture: ComponentFixture<SidenavListComponent>;
  let debugElement: DebugElement;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SidenavListComponent],
      imports: [
        MatButtonModule,
        MatSidenavModule,
        MatCheckboxModule,
        MatGridListModule,
        MatListModule,
        MatIconModule,
        BrowserModule,
        RouterModule,
        RouterTestingModule
      ],
      providers: [
        RequestInProgressService,
        {
          provide: SidenavService,
          useValue: SidenavServiceSpy
        },
        { provide: KadaiEngineService, useValue: KadaiEngineServiceSpy },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SidenavListComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show all links if user has all permissions', () => {
    component.administrationAccess = true;
    component.monitorAccess = true;
    component.workplaceAccess = true;
    component.historyAccess = true;
    fixture.detectChanges();
    const menuList = debugElement.queryAll(By.css('.navlist__item'));
    expect(menuList.length).toBe(10);
    fixture.detectChanges();
  });

  it('should show all links if user has only monitor access', () => {
    component.administrationAccess = false;
    component.monitorAccess = true;
    component.workplaceAccess = false;
    component.historyAccess = false;
    component.settingsAccess = false;
    fixture.detectChanges();
    const menuList = debugElement.queryAll(By.css('.navlist__item'));
    expect(menuList.length).toBe(1);
  });

  it('should toggle sidenav when link clicked', () => {
    component.toggle = true;
    fixture.detectChanges();
    const button = debugElement.query(By.css('.navlist__admin-workbaskets')).nativeElement;
    expect(button).toBeTruthy();
    button.click();
    expect(component.toggle).toBe(false);
  });
});
