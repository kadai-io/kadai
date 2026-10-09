import { inject, Directive, forwardRef } from '@angular/core';
import { AsyncValidator, AbstractControl, ValidationErrors, NG_ASYNC_VALIDATORS } from '@angular/forms';
import { Observable } from 'rxjs';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';
import { accessIdExistsValidator } from 'app/shared/validators/access-id-exists.validator';

@Directive({
  selector: '[kadaiAccessIdExists]',
  providers: [
    {
      provide: NG_ASYNC_VALIDATORS,
      useExisting: forwardRef(() => AccessIdExistsValidatorDirective),
      multi: true
    }
  ]
})
export class AccessIdExistsValidatorDirective implements AsyncValidator {
  private accessIdsService = inject(AccessIdsService);

  validate(control: AbstractControl): Observable<ValidationErrors | null> {
    return accessIdExistsValidator(this.accessIdsService)(control) as Observable<ValidationErrors | null>;
  }
}
