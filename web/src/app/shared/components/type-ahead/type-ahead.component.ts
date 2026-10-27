/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

import { AfterContentInit, Component, computed, DestroyRef, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ControlValueAccessor, FormControl, NgControl, ReactiveFormsModule } from '@angular/forms';
import { Store } from '@ngxs/store';
import { merge, Observable, of, Subject, timer } from 'rxjs';
import { catchError, map, switchMap, takeUntil } from 'rxjs/operators';
import { NgClass } from '@angular/common';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';
import { MatTooltip, TooltipPosition } from '@angular/material/tooltip';
import { MatInput } from '@angular/material/input';
import { MatAutocomplete, MatAutocompleteSelectedEvent, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { ErrorStateMatcher, MatOption } from '@angular/material/core';
import { AccessIdsService } from '../../services/access-ids/access-ids.service';
import { AccessId } from '../../models/access-id';
import { EngineConfigurationSelectors } from '../../store/engine-configuration-store/engine-configuration.selectors';

export const DEFAULT_LOOKUP_DEBOUNCE_MS = 750;

export type TypeAheadSize = 'compact' | 'regular';

type LookupResult =
  | { status: 'empty' }
  | { status: 'found'; accessIds: AccessId[]; match: AccessId }
  | { status: 'not-found'; accessIds: AccessId[] }
  | { status: 'failed' };

@Component({
  selector: 'kadai-shared-type-ahead',
  templateUrl: './type-ahead.component.html',
  styleUrls: ['./type-ahead.component.scss'],
  imports: [
    ReactiveFormsModule,
    NgClass,
    MatFormField,
    MatLabel,
    MatTooltip,
    MatInput,
    MatAutocompleteTrigger,
    MatError,
    MatAutocomplete,
    MatOption
  ]
})
export class TypeAheadComponent implements ControlValueAccessor, AfterContentInit {
  placeholder = input('');
  savedName = input('');
  size = input<TypeAheadSize>('regular');
  required = input(false);
  displayError = input(false);
  emitEmptySelection = input(true);
  matTooltipPosition = input<TooltipPosition>('below');
  accessIdSelected = output<AccessId | null>();

  readonly inputControl = new FormControl('', { nonNullable: true });
  readonly name = computed(() => this.foundName() || (this.showsWrittenValue() ? this.savedName() : ''));
  readonly suggestedAccessIds = signal<AccessId[]>([]);
  readonly lookupResult = signal<LookupResult>({ status: 'empty' });
  readonly debounceMs = computed(() => this.debounceConfig() || DEFAULT_LOOKUP_DEBOUNCE_MS);
  readonly hasError = computed(() => {
    if (!this.displayError()) {
      return false;
    }
    this.controlState();
    const control = this.ngControl?.control;
    if (control) {
      return control.invalid && (control.touched || control.dirty);
    }
    const result = this.lookupResult();
    return result.status === 'not-found' || result.status === 'failed';
  });
  readonly errorMessage = computed(() => {
    this.controlState();
    return this.ngControl?.control?.hasError('required') ? 'Access id is required' : 'Access id not valid';
  });
  readonly errorStateMatcher: ErrorStateMatcher = { isErrorState: () => this.hasError() };

  private readonly ngControl = inject(NgControl, { self: true, optional: true });
  private readonly accessIdsService = inject(AccessIdsService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly debounceConfig = toSignal(
    inject(Store)
      .select(EngineConfigurationSelectors.globalCustomisation)
      .pipe(map((customisation) => customisation?.debounceTimeLookupField)),
    { initialValue: undefined }
  );
  private readonly foundName = signal('');
  private readonly showsWrittenValue = signal(false);
  private readonly written$ = new Subject<void>();
  private readonly committed$ = new Subject<string>();
  private readonly lookup$ = new Subject<string>();
  private readonly controlState = signal(0);
  private lastCommittedValue = '';
  private lastLookedUpValue: string | undefined = undefined;
  private writtenValue = '';
  private selection: AccessId | null = null;
  // undefined: the parent holds its own data for the current value (initial state or after writeValue).
  private lastEmitted: AccessId | null | undefined = undefined;
  private onChange: (value: string | null) => void = () => {};
  private onTouched: () => void = () => {};

  constructor() {
    if (this.ngControl) {
      this.ngControl.valueAccessor = this;
    }

    const typed$ = this.inputControl.valueChanges.pipe(
      switchMap((value) =>
        value
          ? timer(this.debounceMs()).pipe(
              map(() => value),
              takeUntil(this.written$)
            )
          : of(value)
      )
    );

    merge(typed$, this.committed$)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        const isNewValue = value !== this.lastCommittedValue;
        if (isNewValue) {
          this.lastCommittedValue = value;
          this.showsWrittenValue.set(value === this.writtenValue);
          this.onChange(value || null);
          // The name of the previous selection must never stay attached to a new value, even until the lookup answers.
          if (this.selection?.accessId !== value) {
            this.resetSelection();
            this.emitSelection(null);
          }
        }
        // A cancelled lookup never advances lastLookedUpValue, so returning to the committed value looks it up again.
        if (isNewValue || value !== this.lastLookedUpValue) {
          this.lookup$.next(value);
        }
      });

    this.lookup$
      .pipe(
        switchMap((value) =>
          this.lookup(value).pipe(
            map((result) => ({ value, result })),
            takeUntil(this.inputControl.valueChanges)
          )
        ),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(({ value, result }) => {
        this.lastLookedUpValue = value;
        this.applyLookupResult(result);
      });
  }

  ngAfterContentInit() {
    this.ngControl?.control?.events
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.controlState.update((state) => state + 1));
  }

  writeValue(value: string | null): void {
    const accessId = value ?? '';
    this.written$.next();
    this.inputControl.setValue(accessId, { emitEvent: false });
    this.lastCommittedValue = accessId;
    this.writtenValue = accessId;
    this.showsWrittenValue.set(!!accessId);
    if (this.selection?.accessId !== accessId) {
      this.resetSelection();
      this.lastEmitted = undefined;
    }
    this.lookup$.next(accessId);
  }

  registerOnChange(fn: (value: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    if (isDisabled) {
      this.inputControl.disable({ emitEvent: false });
    } else {
      this.inputControl.enable({ emitEvent: false });
    }
  }

  onBlur(): void {
    this.committed$.next(this.inputControl.value);
    this.onTouched();
  }

  onOptionSelected(event: MatAutocompleteSelectedEvent): void {
    this.committed$.next(event.option.value);
  }

  private lookup(value: string): Observable<LookupResult> {
    if (!value) {
      return of({ status: 'empty' });
    }
    return this.accessIdsService.searchForAccessId(value).pipe(
      map((accessIds): LookupResult => {
        const match = accessIds.find((item) => item.accessId?.toLowerCase() === value.toLowerCase());
        return match ? { status: 'found', accessIds, match } : { status: 'not-found', accessIds };
      }),
      catchError(() => of<LookupResult>({ status: 'failed' }))
    );
  }

  // Only a found match changes the selection. A value missing from the search results may still be a valid
  // access id (the search needs at least 3 characters and its results are capped), so it keeps the parent's data.
  private applyLookupResult(result: LookupResult): void {
    this.lookupResult.set(result);
    this.suggestedAccessIds.set(result.status === 'found' || result.status === 'not-found' ? result.accessIds : []);
    if (result.status !== 'found') {
      return;
    }
    this.foundName.set(result.match.name ?? '');
    this.selection = result.match;
    this.emitSelection(result.match);
  }

  private emitSelection(selection: AccessId | null): void {
    if (!selection && !this.emitEmptySelection()) {
      return;
    }
    if (this.lastEmitted !== undefined && this.lastEmitted?.accessId === selection?.accessId) {
      return;
    }
    this.lastEmitted = selection;
    this.accessIdSelected.emit(selection);
  }

  private resetSelection(): void {
    this.selection = null;
    this.foundName.set('');
  }
}
