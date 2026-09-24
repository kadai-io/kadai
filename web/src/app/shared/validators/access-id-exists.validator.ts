import { AsyncValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { map, catchError, first } from 'rxjs/operators';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';

export function accessIdExistsValidator(accessIdsService: AccessIdsService): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) {
      return of(null);
    }

    const value = typeof control.value === 'string' ? control.value : control.value.accessId;

    return accessIdsService.validateAccessId(value).pipe(
      map((isValid) => (isValid ? null : { invalidAccessId: true })),
      catchError(() => of({ accessIdLookupError: true })),
      first()
    );
  };
}
