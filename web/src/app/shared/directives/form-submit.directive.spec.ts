import { Component, viewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormSubmitDirective } from './form-submit.directive';
import { expect, it, beforeEach, describe } from 'vitest';

@Component({
  imports: [FormSubmitDirective],
  template: `
    <form kadaiFormSubmit #formDirective="kadaiFormSubmit" (submit)="onFormSubmit($event)">
      <button type="submit" id="submit-btn">Submit</button>
    </form>
  `
})
class TestComponent {
  readonly formDirective = viewChild.required(FormSubmitDirective);

  onFormSubmit(event: Event): void {
    event.preventDefault();
  }
}

describe('FormSubmitDirective', () => {
  let fixture: ComponentFixture<TestComponent>;
  let hostComponent: TestComponent;
  let directive: FormSubmitDirective;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestComponent, FormSubmitDirective]
    }).compileComponents();

    fixture = TestBed.createComponent(TestComponent);
    hostComponent = fixture.componentInstance;
    fixture.detectChanges();

    directive = hostComponent.formDirective();
  });

  it('should create an instance', () => {
    expect(directive).toBeTruthy();
  });

  it('should have initial isSubmitted state as false', () => {
    expect(directive.isSubmitted()).toBeFalsy();
  });

  it('should set isSubmitted to true when form is submitted', () => {
    const formElement: HTMLFormElement = fixture.nativeElement.querySelector('form');

    formElement.dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    expect(directive.isSubmitted()).toBeTruthy();
  });

  it('should reset isSubmitted state to false when resetSubmitState is called', () => {
    directive.onSubmit();
    expect(directive.isSubmitted()).toBeTruthy();

    directive.resetSubmitState();
    fixture.detectChanges();

    expect(directive.isSubmitted()).toBeFalsy();
  });
});
