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
import { UserInformationComponent } from './user-information.component';
import { By } from '@angular/platform-browser';
import { KadaiEngineService } from '../../services/kadai-engine/kadai-engine.service';
import { KadaiEngineServiceMock } from '../../services/kadai-engine/kadai-engine.mock.service';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

jest.mock('angular-svg-icon');

describe('UserInformationComponent', () => {
  let component: UserInformationComponent;
  let fixture: ComponentFixture<UserInformationComponent>;
  let debugElement: DebugElement;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [UserInformationComponent],
      providers: [{ provide: KadaiEngineService, useClass: KadaiEngineServiceMock }, provideAnimationsAsync()]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserInformationComponent);
    debugElement = fixture.debugElement;
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle roles when roles clicked', () => {
    fixture.detectChanges();
    expect(component.showRoles).toBe(false);
    const button = debugElement.query(By.css('.user-info__button')).nativeElement;
    expect(button).toBeTruthy();
    button.click();
    expect(component.showRoles).toBe(true);
  });
});
