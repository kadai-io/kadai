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

import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, DebugElement, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IconTypeComponent } from './icon-type.component';
import { WorkbasketType } from '../../../shared/models/workbasket-type';
import { MatTooltip } from '@angular/material/tooltip';
import { By } from '@angular/platform-browser';

describe('IconTypeComponent', () => {
  let fixture: ComponentFixture<IconTypeComponent>;
  let debugElement: DebugElement;
  let component: IconTypeComponent;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [IconTypeComponent],
      declarations: [],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
    }).compileComponents();

    TestBed.overrideComponent(IconTypeComponent, {
      set: { imports: [MatTooltip], schemas: [NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA] }
    });

    fixture = TestBed.createComponent(IconTypeComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
  }));

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should return icon path dependent on the type when calling getIconPath', () => {
    expect(component.getIconPath(WorkbasketType.PERSONAL)).toBe('user.svg');
    expect(component.getIconPath(WorkbasketType.GROUP)).toBe('users.svg');
    expect(component.getIconPath(WorkbasketType.TOPIC)).toBe('topic.svg');
    expect(component.getIconPath(WorkbasketType.CLEARANCE)).toBe('clearance.svg');
    expect(component.getIconPath(undefined)).toBe('asterisk.svg');
  });

  it('should display svg-icon', () => {
    expect(debugElement.nativeElement.querySelector('svg-icon')).toBeTruthy();
  });

  it('should set iconSize on init based on size input', () => {
    const cmpSmall = TestBed.createComponent(IconTypeComponent).componentInstance;
    cmpSmall.size = 'small';
    cmpSmall.ngOnInit();
    expect(cmpSmall.iconSize).toBe('16');

    const cmpLarge = TestBed.createComponent(IconTypeComponent).componentInstance;
    cmpLarge.size = 'large';
    cmpLarge.ngOnInit();
    expect(cmpLarge.iconSize).toBe('24');
  });

  it('should set iconColor on changes when selected changes to true/false', () => {
    component.ngOnChanges({
      selected: new SimpleChange(false, true, false)
    });
    expect(component.iconColor).toBe('white');

    component.ngOnChanges({
      selected: new SimpleChange(true, false, false)
    });
    expect(component.iconColor).toBe('#555');
  });

  it('should not modify iconColor when selected is not part of changes', () => {
    component.iconColor = 'initial';
    component.ngOnChanges({});
    expect(component.iconColor).toBe('#555');
  });

  it('should render provided text', () => {
    component.text = 'Hello Icon';
    fixture.detectChanges();
    expect(debugElement.nativeElement.textContent).toContain('Hello Icon');
  });

  it('should bind tooltip text only when tooltip is true', () => {
    component.tooltip = false;
    component.type = WorkbasketType.PERSONAL as any;
    fixture.detectChanges();
    const tooltipDir1 = debugElement.query(By.directive(MatTooltip))?.injector.get(MatTooltip);
    expect(tooltipDir1?.message ?? '').toBe('');

    component.tooltip = true;
    component.type = WorkbasketType.GROUP as any;
    fixture.detectChanges();
    const tooltipDir2 = debugElement.query(By.directive(MatTooltip))?.injector.get(MatTooltip);
    expect(tooltipDir2?.message).toBe(String(WorkbasketType.GROUP));
  });
});
