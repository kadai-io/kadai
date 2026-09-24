import { Directive, inject, signal } from '@angular/core';
import { NgControl } from '@angular/forms';
import { FormSubmitDirective } from './form-submit.directive';

@Directive({
  selector: '[kadaiFormFieldSubmit]',
  exportAs: 'kadaiFormField',
  host: {
    '(blur)': 'onInteraction()',
    '(input)': 'onInteraction()'
  }
})
export class FormFieldSubmitDirective {
  private readonly ngControl = inject(NgControl, { optional: true });
  private readonly formSubmit = inject(FormSubmitDirective, { optional: true });

  private readonly isInteracted = signal(false);

  onInteraction(): void {
    if (!this.isInteracted()) {
      this.isInteracted.set(true);
    }
  }

  get hasError(): boolean {
    const control = this.ngControl?.control;
    if (!control) {
      return false;
    }

    const isTouched = control.touched || this.isInteracted();
    const isSubmitted = this.formSubmit?.isSubmitted() ?? false;

    return control.invalid && (isTouched || isSubmitted);
  }
}
