import { AsyncValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { map, catchError, switchMap, first } from 'rxjs/operators';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';

export function accessIdExistsValidator(accessIdsService: AccessIdsService): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) {
      return of(null);
    }

    const value = typeof control.value === 'string' ? control.value : control.value.accessId;

    return of(value).pipe(
      switchMap((searchVal) => accessIdsService.searchForAccessId(searchVal)),
      map((items) => {
        const isValid = items?.some((item) => item.accessId?.toLowerCase() === value?.toLowerCase());
        return isValid ? null : { invalidAccessId: true };
      }),
      catchError(() => of({ accessIdLookupError: true })),
      first()
    );
  };
}