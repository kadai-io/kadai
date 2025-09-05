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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { SidenavListComponent } from './sidenav-list.component';
import { By } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';
import { EMPTY } from 'rxjs';

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
      imports: [SidenavListComponent],
      providers: [provideRouter([]), { provide: KadaiEngineService, useValue: KadaiEngineServiceSpy }]
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
