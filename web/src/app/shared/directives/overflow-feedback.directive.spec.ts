import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { OverflowFeedbackDirective } from './overflow-feedback.directive';

@Component({
  imports: [OverflowFeedbackDirective],
  template: `<input [kadaiOverflowFeedback]="10" type="text" />`
})
class TestHostComponent {}

describe('OverflowFeedbackDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let directive: OverflowFeedbackDirective;
  let inputEl: HTMLInputElement;

  beforeEach(() => {
    vi.useFakeTimers();

    TestBed.configureTestingModule({
      imports: [TestHostComponent]
    });

    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const inputDebugEl = fixture.debugElement.query(By.directive(OverflowFeedbackDirective));
    directive = inputDebugEl.injector.get(OverflowFeedbackDirective);
    inputEl = inputDebugEl.nativeElement as HTMLInputElement;
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  // helper function for the simulation of user input
  function typeValue(value: string): void {
    inputEl.value = value;
    inputEl.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  it('should not set isOverflowed when value length is within limit', () => {
    typeValue('12345');
    expect(directive.isOverflowed()).toBe(false);
  });

  it('should set isOverflowed to true when length reaches/exceeds limit and reset after 3 seconds', () => {
    typeValue('12345678901');
    expect(directive.isOverflowed()).toBe(true);

    vi.advanceTimersByTime(2999);
    expect(directive.isOverflowed()).toBe(true);

    vi.advanceTimersByTime(1);
    expect(directive.isOverflowed()).toBe(false);
  });

  it('should restart timer if new input occurs during active overflow state', () => {
    typeValue('12345678901');
    expect(directive.isOverflowed()).toBe(true);

    vi.advanceTimersByTime(2000);

    typeValue('123456789012');

    vi.advanceTimersByTime(1500);
    expect(directive.isOverflowed()).toBe(true);

    vi.advanceTimersByTime(1500);
    expect(directive.isOverflowed()).toBe(false);
  });

  it('should immediately set isOverflowed to false if text length drops below limit', () => {
    typeValue('12345678901');
    expect(directive.isOverflowed()).toBe(true);

    typeValue('123');
    expect(directive.isOverflowed()).toBe(false);
  });
});
