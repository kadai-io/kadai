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
import { FieldErrorDisplayComponent } from './field-error-display.component';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('FieldErrorDisplayComponent', () => {
  let component: FieldErrorDisplayComponent;
  let fixture: ComponentFixture<FieldErrorDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FieldErrorDisplayComponent],
      providers: [provideAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(FieldErrorDisplayComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should have displayError as undefined by default', () => {
    fixture.detectChanges();
    expect(component.displayError).toBeUndefined();
  });

  it('should have errorMessage as undefined by default', () => {
    fixture.detectChanges();
    expect(component.errorMessage).toBeUndefined();
  });

  it('should have validationTrigger as undefined by default', () => {
    fixture.detectChanges();
    expect(component.validationTrigger).toBeUndefined();
  });

  it('should accept displayError input set to true', () => {
    component.displayError = true;
    component.validationTrigger = false;
    fixture.detectChanges();
    expect(component.displayError).toBe(true);
  });

  it('should accept displayError input set to false', () => {
    component.displayError = false;
    fixture.detectChanges();
    expect(component.displayError).toBe(false);
  });

  it('should accept errorMessage input', () => {
    component.errorMessage = 'Field is required';
    fixture.detectChanges();
    expect(component.errorMessage).toBe('Field is required');
  });

  it('should accept validationTrigger input', () => {
    component.validationTrigger = true;
    component.displayError = false;
    fixture.detectChanges();
    expect(component.validationTrigger).toBe(true);
  });

  it('should hold all three input values simultaneously when displayError is true', () => {
    component.displayError = true;
    component.errorMessage = 'This field is required';
    component.validationTrigger = false;
    fixture.detectChanges();

    expect(component.displayError).toBe(true);
    expect(component.errorMessage).toBe('This field is required');
    expect(component.validationTrigger).toBe(false);
  });
});
