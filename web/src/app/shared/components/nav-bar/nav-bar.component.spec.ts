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
import { NavBarComponent } from './nav-bar.component';
import { By } from '@angular/platform-browser';

jest.mock('angular-svg-icon');

describe('NavBarComponent', () => {
  let component: NavBarComponent;
  let fixture: ComponentFixture<NavBarComponent>;
  let debugElement: DebugElement;
  let route = '';

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [NavBarComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavBarComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set title to workbasket if workbasket ist selected', () => {
    route = 'administration';
    fixture.detectChanges();
    component.setTitle(route);
    expect(component.title).toBe('Administration');
  });
  it('should set title to monitor if monitor ist selected', () => {
    route = 'monitor';
    fixture.detectChanges();
    component.setTitle(route);
    expect(component.title).toBe('Monitor');
  });

  it('should set title to workplace if workplace ist selected', () => {
    route = 'workplace';
    fixture.detectChanges();
    component.setTitle(route);
    expect(component.title).toBe('Workplace');
  });

  it('should set title to history if history ist selected', () => {
    route = 'history';
    fixture.detectChanges();
    component.setTitle(route);
    expect(component.title).toBe('History');
  });

  it('should set title to settings if settings ist selected', () => {
    route = 'settings';
    fixture.detectChanges();
    component.setTitle(route);
    expect(component.title).toBe('Settings');
  });

  it('should toggle sidenav when button clicked', () => {
    fixture.detectChanges();
    expect(component.toggle).toBe(false);
    const button = debugElement.query(By.css('.navbar_button-toggle')).nativeElement;
    expect(button).toBeTruthy();
    button.click();
    expect(component.toggle).toBe(true);
  });
});
