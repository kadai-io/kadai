import { Directive, signal } from '@angular/core';

@Directive({
  selector: 'form[kadaiFormSubmit], ng-form[kadaiFormSubmit]',
  exportAs: 'kadaiFormSubmit',
  host: { '(submit)': 'onSubmit()' }
})
export class FormSubmitDirective {
  readonly isSubmitted = signal(false);

  onSubmit(): void {
    this.isSubmitted.set(true);
  }

  resetSubmitState(): void {
    this.isSubmitted.set(false);
  }
}
