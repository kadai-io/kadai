import { Directive, computed, inject, signal } from '@angular/core';
import { NgControl } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormSubmitDirective } from './form-submit.directive';
import { of } from 'rxjs';

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

  // readonly hasError = computed(() => {
  //   const control = this.ngControl?.control;
  //   if (!control) {
  //     return false;
  //   }

  //   const isTouched = control.touched || this.isInteracted();
  //   const isSubmitted = this.formSubmit?.isSubmitted() ?? false;

  //   return control.invalid && (isTouched || isSubmitted);
  // });
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
