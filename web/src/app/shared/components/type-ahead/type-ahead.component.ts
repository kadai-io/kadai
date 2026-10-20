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
import { catchError, filter, map, switchMap, takeUntil } from 'rxjs/operators';
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
  size = input<TypeAheadSize>('regular');
  required = input(false);
  displayError = input(false);
  emitEmptySelection = input(true);
  matTooltipPosition = input<TooltipPosition>('below');
  accessIdSelected = output<AccessId | null>();

  readonly inputControl = new FormControl('', { nonNullable: true });
  readonly name = signal('');
  readonly filteredAccessIds = signal<AccessId[]>([]);
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
  private readonly written$ = new Subject<void>();
  private readonly committed$ = new Subject<string>();
  private readonly lookup$ = new Subject<string>();
  private readonly controlState = signal(0);
  private lastCommittedValue = '';
  private selection: AccessId | null = null;
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
      .pipe(
        filter((value) => value !== this.lastCommittedValue),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((value) => {
        this.lastCommittedValue = value;
        this.onChange(value || null);
        this.lookup$.next(value);
      });

    this.lookup$
      .pipe(
        switchMap((value) => this.lookup(value)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((result) => this.applyLookupResult(result));
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

  private applyLookupResult(result: LookupResult): void {
    this.lookupResult.set(result);
    this.filteredAccessIds.set(result.status === 'found' || result.status === 'not-found' ? result.accessIds : []);
    const selection = result.status === 'found' ? result.match : null;
    this.name.set(selection?.name ?? '');
    if (selection?.accessId === this.selection?.accessId) {
      return;
    }
    this.selection = selection;
    if (selection || this.emitEmptySelection()) {
      this.accessIdSelected.emit(selection);
    }
  }
}
