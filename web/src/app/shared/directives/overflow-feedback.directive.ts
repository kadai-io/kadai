import { Directive, input, OnDestroy, signal } from '@angular/core';

@Directive({
  selector: '[kadaiOverflowFeedback]',
  exportAs: 'overflowFeedback',
  host: { '(input)': 'onInput($event)' }
})
export class OverflowFeedbackDirective implements OnDestroy {
  maxLength = input<number>(0, { alias: 'kadaiOverflowFeedback' });
  readonly isOverflowed = signal<boolean>(false);
  private timeoutId?: ReturnType<typeof setTimeout>;

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement | HTMLTextAreaElement | null;
    const value = target?.value ?? '';
    const limit = this.maxLength();

    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }

    if (limit > 0 && value.length >= limit) {
      this.isOverflowed.set(true);
      this.timeoutId = setTimeout(() => this.isOverflowed.set(false), 3000);
    } else {
      this.isOverflowed.set(false);
    }
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  private clearTimer(): void {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
      this.timeoutId = undefined;
    }
  }
}
