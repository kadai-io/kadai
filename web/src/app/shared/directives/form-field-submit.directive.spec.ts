import { Component, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormSubmitDirective } from './form-submit.directive';
import { FormFieldSubmitDirective } from './form-field-submit.directive';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { it, describe, beforeEach, expect } from 'vitest';

@Component({
  imports: [ReactiveFormsModule, FormSubmitDirective, FormFieldSubmitDirective],
  template: `
    <form kadaiFormSubmit>
      <input id="test-input" [formControl]="control" kadaiFormFieldSubmit #fieldDirective="kadaiFormField" />
      <button type="submit" id="submit-btn">Submit</button>
    </form>
  `
})
class TestComponent {
  readonly control = new FormControl('', { validators: [Validators.required] });
  readonly fieldDirective = viewChild.required(FormFieldSubmitDirective);
  readonly formDirective = viewChild.required(FormSubmitDirective);
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, FormFieldSubmitDirective],
  template: `
    <input id="standalone-input" [formControl]="control" kadaiFormFieldSubmit #fieldDirective="kadaiFormField" />
  `
})
class StandaloneInputComponent {
  readonly control = new FormControl('', { validators: [Validators.required] });
  readonly fieldDirective = viewChild.required(FormFieldSubmitDirective);
}

describe('FormFieldSubmitDirective', () => {
  describe('Integration with FormSubmitDirective and NgControl', () => {
    let fixture: ComponentFixture<TestComponent>;
    let hostComponent: TestComponent;
    let fieldDirective: FormFieldSubmitDirective;
    let inputElement: HTMLInputElement;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [TestComponent]
      }).compileComponents();

      fixture = TestBed.createComponent(TestComponent);
      hostComponent = fixture.componentInstance;
      fixture.detectChanges();

      fieldDirective = hostComponent.fieldDirective();
      inputElement = fixture.nativeElement.querySelector('#test-input');
    });

    it('should create an instance', () => {
      expect(fieldDirective).toBeTruthy();
    });

    it('should initially have hasError as false when control is pristine and untouched', () => {
      expect(hostComponent.control.invalid).toBeTruthy();
      expect(fieldDirective.hasError).toBeFalsy();
    });

    it('should set hasError to true when user triggers blur event on invalid control', () => {
      inputElement.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      expect(fieldDirective.hasError).toBeTruthy();
    });

    it('should set hasError to true when user triggers input event on invalid control', () => {
      inputElement.dispatchEvent(new Event('input'));
      fixture.detectChanges();

      expect(fieldDirective.hasError).toBeTruthy();
    });

    it('should set hasError to true if form is submitted even if input was untouched', () => {
      const formElement: HTMLFormElement = fixture.nativeElement.querySelector('form');

      formElement.dispatchEvent(new Event('submit'));
      fixture.detectChanges();

      expect(hostComponent.formDirective().isSubmitted()).toBeTruthy();
      expect(fieldDirective.hasError).toBeTruthy();
    });

    it('should return hasError as false when control becomes valid after interaction', async () => {
      inputElement.dispatchEvent(new Event('blur'));
      fixture.detectChanges();
      expect(fieldDirective.hasError).toBeTruthy();

      hostComponent.control.setValue('Valid Value');
      inputElement.dispatchEvent(new Event('input'));
      await fixture.detectChanges();

      expect(hostComponent.control.valid).toBeTruthy();
      expect(fieldDirective.hasError).toBeFalsy();
    });

    it('should handle control.touched property set programmatically', () => {
      hostComponent.control.markAsTouched();
      fixture.detectChanges();

      expect(fieldDirective.hasError).toBeTruthy();
    });
  });

  describe('Standalone usage (without FormSubmitDirective)', () => {
    let fixture: ComponentFixture<StandaloneInputComponent>;
    let hostComponent: StandaloneInputComponent;
    let fieldDirective: FormFieldSubmitDirective;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [StandaloneInputComponent]
      }).compileComponents();

      fixture = TestBed.createComponent(StandaloneInputComponent);
      hostComponent = fixture.componentInstance;
      fixture.detectChanges();

      fieldDirective = hostComponent.fieldDirective();
    });

    it('should work without parent FormSubmitDirective', () => {
      expect(fieldDirective.hasError).toBeFalsy();

      const inputElement: HTMLInputElement = fixture.nativeElement.querySelector('#standalone-input');
      inputElement.dispatchEvent(new Event('blur'));
      fixture.detectChanges();

      expect(fieldDirective.hasError).toBeTruthy();
    });
  });
});
