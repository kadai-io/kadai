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
import { beforeEach, describe, expect, it } from 'vitest';
import { DialogPopUpComponent } from './dialog-pop-up.component';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

describe('DialogPopUpComponent - with data', () => {
  let component: DialogPopUpComponent;
  let fixture: ComponentFixture<DialogPopUpComponent>;
  const mockCallback = () => {};

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DialogPopUpComponent, MatDialogModule],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: { message: 'Test message', callback: mockCallback }
        },
        provideNoopAnimations()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DialogPopUpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set message and callback from data', () => {
    expect(component.message).toBe('Test message');
    expect(component.callback).toBe(mockCallback);
    expect(component.isDataSpecified).toBeTruthy();
  });
});

describe('DialogPopUpComponent - without data', () => {
  let component: DialogPopUpComponent;
  let fixture: ComponentFixture<DialogPopUpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DialogPopUpComponent, MatDialogModule],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {}
        },
        provideNoopAnimations()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DialogPopUpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set fallback message when data has no message or callback', () => {
    expect(component.isDataSpecified).toBeFalsy();
    // The message should come from ObtainMessageService for POPUP_CONFIGURATION key
    expect(component.message).toBeDefined();
    expect(typeof component.message).toBe('string');
  });
});
