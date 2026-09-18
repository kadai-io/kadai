import { inject, Directive, forwardRef } from '@angular/core';
import { AsyncValidator, AbstractControl, ValidationErrors, NG_ASYNC_VALIDATORS } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { map, catchError, switchMap, first } from 'rxjs/operators';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';

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
    if (!control.value) {
      return of(null);
    }

    const value = typeof control.value === 'string' ? control.value : control.value.accessId;

    return of(value).pipe(
      switchMap((searchVal) => this.accessIdsService.searchForAccessId(searchVal)),
      map((items) => {
        const isValid = items?.some((item) => item.accessId?.toLowerCase() === value?.toLowerCase());
        return isValid ? null : { invalidAccessId: true };
      }),
      catchError(() => of({ accessIdLookupError: true })),
      first()
    );
  }
}
