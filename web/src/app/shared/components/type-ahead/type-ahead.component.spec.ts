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

import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { Store } from '@ngxs/store';
import { BehaviorSubject, Observable, of, Subject, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { DEFAULT_LOOKUP_DEBOUNCE_MS, TypeAheadComponent, TypeAheadSize } from './type-ahead.component';
import { AccessIdsService } from '../../services/access-ids/access-ids.service';
import { AccessId } from '../../models/access-id';
import { GlobalCustomisation } from '../../models/customisation';

const DEBOUNCE_MS = 500;

@Component({
  imports: [TypeAheadComponent, ReactiveFormsModule],
  template: `
    <kadai-shared-type-ahead
      (accessIdSelected)="selected.push($event)"
      [displayError]="displayError()"
      [emitEmptySelection]="emitEmptySelection()"
      [formControl]="control"
      [required]="required()"
      [savedName]="savedName()"
      [size]="size()"
      placeholder="Owner"
    />
  `
})
class ReactiveHostComponent {
  control = new FormControl<string | null>(null);
  displayError = signal(true);
  emitEmptySelection = signal(true);
  required = signal(false);
  savedName = signal('');
  size = signal<TypeAheadSize>('regular');
  selected: (AccessId | null)[] = [];
}

@Component({
  imports: [TypeAheadComponent],
  template: `<kadai-shared-type-ahead (accessIdSelected)="selected.push($event)" [displayError]="true" />`
})
class StandaloneHostComponent {
  selected: (AccessId | null)[] = [];
}

describe('TypeAheadComponent', () => {
  let searchForAccessId: ReturnType<typeof vi.fn>;
  let globalCustomisation$: BehaviorSubject<GlobalCustomisation | undefined>;

  const users: AccessId[] = [
    { accessId: 'user-1-1', name: 'Max Mustermann' },
    { accessId: 'user-1-2', name: 'Erika Musterfrau' }
  ];

  beforeEach(() => {
    vi.useFakeTimers();
    searchForAccessId = vi.fn((value: string) =>
      of(users.filter((user) => user.accessId!.startsWith(value.toLowerCase())))
    );
    globalCustomisation$ = new BehaviorSubject<GlobalCustomisation | undefined>({
      debounceTimeLookupField: DEBOUNCE_MS
    } as GlobalCustomisation);

    TestBed.configureTestingModule({
      providers: [
        { provide: AccessIdsService, useValue: { searchForAccessId } },
        { provide: Store, useValue: { select: () => globalCustomisation$ } }
      ]
    });
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  describe('as a form control', () => {
    let fixture: ComponentFixture<ReactiveHostComponent>;
    let host: ReactiveHostComponent;
    let typeAhead: TypeAheadComponent;

    const input = (): HTMLInputElement => fixture.nativeElement.querySelector('input');
    const type = (value: string) => {
      input().value = value;
      input().dispatchEvent(new Event('input'));
      fixture.detectChanges();
    };
    const errorText = () => fixture.nativeElement.querySelector('mat-error')?.textContent?.trim();
    // MatAutocompleteTrigger writes model values to the input element in a microtask.
    const settle = async () => {
      await Promise.resolve();
      fixture.detectChanges();
    };

    beforeEach(() => {
      fixture = TestBed.createComponent(ReactiveHostComponent);
      host = fixture.componentInstance;
      fixture.detectChanges();
      typeAhead = fixture.debugElement.query(By.directive(TypeAheadComponent)).componentInstance;
      searchForAccessId.mockClear();
    });

    describe('external writes', () => {
      it('should show an externally written value and look up its name without updating the parent', async () => {
        const valueChanges = vi.fn();
        host.control.valueChanges.subscribe(valueChanges);

        host.control.setValue('user-1-1');
        await settle();

        expect(input().value).toBe('user-1-1');
        expect(searchForAccessId).toHaveBeenCalledWith('user-1-1');
        expect(typeAhead.name()).toBe('Max Mustermann');
        expect(host.selected).toEqual([users[0]]);
        expect(valueChanges).toHaveBeenCalledTimes(1);
      });

      it('should clear the field when null is written without emitting a selection', async () => {
        host.control.setValue('user-1-1');
        host.control.setValue(null);
        await settle();

        expect(input().value).toBe('');
        expect(typeAhead.name()).toBe('');
        expect(host.selected).toEqual([users[0]]);
      });

      it('should restore the previous value on reset (undo)', async () => {
        host.control.setValue('user-1-1');
        type('user-1-2');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        expect(typeAhead.name()).toBe('Erika Musterfrau');

        host.control.reset('user-1-1');
        await settle();

        expect(input().value).toBe('user-1-1');
        expect(typeAhead.name()).toBe('Max Mustermann');
      });
    });

    describe('typing', () => {
      it('should propagate the value and look it up only after the configured debounce time', () => {
        type('user-1-2');

        vi.advanceTimersByTime(DEBOUNCE_MS - 1);
        expect(host.control.value).toBeNull();
        expect(searchForAccessId).not.toHaveBeenCalled();

        vi.advanceTimersByTime(1);
        expect(host.control.value).toBe('user-1-2');
        expect(searchForAccessId).toHaveBeenCalledTimes(1);
        expect(typeAhead.name()).toBe('Erika Musterfrau');
        expect(host.selected).toEqual([null, users[1]]);
      });

      it('should debounce consecutive input into a single lookup', () => {
        type('user');
        vi.advanceTimersByTime(DEBOUNCE_MS / 2);
        type('user-1');
        vi.advanceTimersByTime(DEBOUNCE_MS / 2);
        type('user-1-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(searchForAccessId).toHaveBeenCalledTimes(1);
        expect(searchForAccessId).toHaveBeenCalledWith('user-1-1');
      });

      it('should match access ids case-insensitively', () => {
        type('USER-1-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(typeAhead.name()).toBe('Max Mustermann');
        expect(host.selected).toEqual([null, users[0]]);
      });

      it('should offer the search results as autocomplete options', () => {
        type('user-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(typeAhead.suggestedAccessIds()).toEqual(users);
        expect(typeAhead.name()).toBe('');
      });

      it('should use the default debounce time when none is configured', () => {
        globalCustomisation$.next({} as GlobalCustomisation);
        type('user-1-1');

        vi.advanceTimersByTime(DEFAULT_LOOKUP_DEBOUNCE_MS - 1);
        expect(searchForAccessId).not.toHaveBeenCalled();
        vi.advanceTimersByTime(1);
        expect(searchForAccessId).toHaveBeenCalledWith('user-1-1');
      });

      it('should apply a debounce configuration that arrives after creation', () => {
        globalCustomisation$.next({ debounceTimeLookupField: 100 } as GlobalCustomisation);
        type('user-1-1');

        vi.advanceTimersByTime(100);
        expect(searchForAccessId).toHaveBeenCalledWith('user-1-1');
      });

      it('should commit the value immediately on blur and mark the control as touched', () => {
        type('user-1-1');
        input().dispatchEvent(new Event('blur'));

        expect(host.control.value).toBe('user-1-1');
        expect(host.control.touched).toBe(true);
        expect(searchForAccessId).toHaveBeenCalledTimes(1);

        vi.advanceTimersByTime(DEBOUNCE_MS);
        expect(searchForAccessId).toHaveBeenCalledTimes(1);
      });

      it('should not look up twice when the response is still pending on blur', () => {
        const pending$ = new Subject<AccessId[]>();
        searchForAccessId.mockReturnValueOnce(pending$);

        type('user-1-1');
        input().dispatchEvent(new Event('blur'));
        expect(searchForAccessId).toHaveBeenCalledTimes(1);

        vi.advanceTimersByTime(DEBOUNCE_MS);
        expect(searchForAccessId).toHaveBeenCalledTimes(1);

        pending$.next([users[0]]);
        expect(typeAhead.name()).toBe('Max Mustermann');
        expect(host.selected).toEqual([null, users[0]]);
      });

      it('should commit an autocomplete option immediately', () => {
        typeAhead.inputControl.setValue('user-1-2');
        fixture.debugElement
          .query(By.css('mat-autocomplete'))
          .triggerEventHandler('optionSelected', { option: { value: 'user-1-2' } } as MatAutocompleteSelectedEvent);

        expect(host.control.value).toBe('user-1-2');
        expect(host.selected).toEqual([null, users[1]]);
      });
    });

    describe('stale responses', () => {
      it('should ignore the response of an older lookup when the value changed', () => {
        const first$ = new Subject<AccessId[]>();
        const second$ = new Subject<AccessId[]>();
        searchForAccessId.mockReturnValueOnce(first$).mockReturnValueOnce(second$);

        type('user-1-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        type('user-1-2');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        second$.next([users[1]]);
        first$.next([users[0]]);
        fixture.detectChanges();

        expect(typeAhead.name()).toBe('Erika Musterfrau');
        expect(host.selected).toEqual([null, users[1]]);
      });

      it('should ignore a response that arrives while the next input is still debounced', () => {
        const first$ = new Subject<AccessId[]>();
        searchForAccessId.mockReturnValueOnce(first$);

        type('user-1-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        type('user-1-2');
        vi.advanceTimersByTime(DEBOUNCE_MS / 2);

        first$.next([users[0]]);
        fixture.detectChanges();

        expect(typeAhead.name()).toBe('');
        expect(host.selected).toEqual([null]);

        vi.advanceTimersByTime(DEBOUNCE_MS / 2);
        expect(typeAhead.name()).toBe('Erika Musterfrau');
        expect(host.selected).toEqual([null, users[1]]);
      });

      it('should look up again when the input returns to the committed value', () => {
        const cancelled$ = new Subject<AccessId[]>();
        searchForAccessId.mockReturnValueOnce(cancelled$);

        type('user-1-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        expect(searchForAccessId).toHaveBeenCalledTimes(1);

        type('user-1-1x');
        type('user-1-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(searchForAccessId).toHaveBeenCalledTimes(2);
        expect(typeAhead.name()).toBe('Max Mustermann');
        expect(host.selected).toEqual([null, users[0]]);
      });

      it('should not let a pending debounced value overwrite a later external write', async () => {
        type('user-1-2');
        host.control.setValue('user-1-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        await settle();

        expect(host.control.value).toBe('user-1-1');
        expect(input().value).toBe('user-1-1');
      });
    });

    describe('clearing', () => {
      it('should propagate null immediately and emit an empty selection', async () => {
        host.control.setValue('user-1-1');
        await settle();
        type('');

        expect(host.control.value).toBeNull();
        expect(typeAhead.name()).toBe('');
        expect(host.selected.at(-1)).toBeNull();
      });

      it('should cancel a pending lookup when the field is cleared', () => {
        type('user-1-1');
        type('');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(searchForAccessId).not.toHaveBeenCalled();
        expect(host.control.value).toBeNull();
      });

      it('should not emit an empty selection when emitEmptySelection is false', () => {
        host.emitEmptySelection.set(false);
        fixture.detectChanges();

        type('unknown');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        type('');

        expect(host.selected).toEqual([]);
      });
    });

    describe('selection', () => {
      it('should invalidate the previous selection as soon as a new value is committed, before the lookup answers', () => {
        host.control.setValue('user-1-1');
        const pending$ = new Subject<AccessId[]>();
        searchForAccessId.mockReturnValueOnce(pending$);

        type('user-1-2');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(host.control.value).toBe('user-1-2');
        expect(host.selected).toEqual([users[0], null]);
        expect(typeAhead.name()).toBe('');

        pending$.next([users[1]]);
        expect(host.selected).toEqual([users[0], null, users[1]]);
      });

      it('should keep the parent data for a written value that is missing from the search results', () => {
        host.control.setValue('ab');
        fixture.detectChanges();

        expect(searchForAccessId).toHaveBeenCalledWith('ab');
        expect(typeAhead.lookupResult().status).toBe('not-found');
        expect(host.selected).toEqual([]);
      });

      it('should display the saved name of a written value that is missing from the search results', () => {
        host.savedName.set('Team');
        host.control.setValue('ab');
        fixture.detectChanges();

        expect(typeAhead.lookupResult().status).toBe('not-found');
        expect(typeAhead.name()).toBe('Team');
      });

      it('should drop the saved name once the user replaces the written value', () => {
        host.savedName.set('Team');
        host.control.setValue('ab');
        type('ac');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(typeAhead.name()).toBe('');
      });

      it('should invalidate the parent data when the user replaces a value that was never found', () => {
        host.control.setValue('ab');
        type('user-1-1');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(host.selected).toEqual([null, users[0]]);
      });
    });

    describe('errors', () => {
      it('should emit an empty selection once when a selected access id no longer matches', () => {
        host.control.setValue('user-1-1');
        type('unknown');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        type('unknown-2');
        vi.advanceTimersByTime(DEBOUNCE_MS);

        expect(host.selected).toEqual([users[0], null]);
        expect(typeAhead.lookupResult().status).toBe('not-found');
      });

      it('should not emit when the initial empty value is written', () => {
        expect(host.selected).toEqual([]);
      });

      it('should handle a failed lookup and keep working afterwards', () => {
        host.control.setValue('user-1-1');
        searchForAccessId.mockReturnValueOnce(throwError(() => new Error('lookup failed')));

        type('user-1-1x');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        expect(typeAhead.lookupResult().status).toBe('failed');
        expect(host.selected).toEqual([users[0], null]);

        type('user-1-2');
        vi.advanceTimersByTime(DEBOUNCE_MS);
        expect(typeAhead.name()).toBe('Erika Musterfrau');
      });

      it('should display the validation error of the parent control once it is touched', () => {
        host.control.setValidators(() => ({ invalidAccessId: true }));
        host.control.setValue('unknown');
        fixture.detectChanges();
        expect(errorText()).toBeUndefined();

        host.control.markAsTouched();
        fixture.detectChanges();
        expect(errorText()).toBe('Access id not valid');
      });

      it('should display a required error of the parent control', () => {
        host.control.setValidators(Validators.required);
        host.control.updateValueAndValidity();
        host.control.markAsTouched();
        fixture.detectChanges();

        expect(errorText()).toBe('Access id is required');
      });

      it('should not display errors when displayError is false', () => {
        host.displayError.set(false);
        host.control.setValidators(Validators.required);
        host.control.updateValueAndValidity();
        host.control.markAsTouched();
        fixture.detectChanges();

        expect(errorText()).toBeUndefined();
      });
    });

    describe('disabled state', () => {
      it('should follow disable and enable of the parent control at runtime', () => {
        host.control.disable();
        fixture.detectChanges();
        expect(input().disabled).toBe(true);

        host.control.enable();
        fixture.detectChanges();
        expect(input().disabled).toBe(false);
      });
    });

    describe('presentation', () => {
      it('should select the layout through the size input', () => {
        expect(fixture.nativeElement.querySelector('.type-ahead--large')).toBeTruthy();

        host.size.set('compact');
        fixture.detectChanges();
        expect(fixture.nativeElement.querySelector('.type-ahead--small')).toBeTruthy();
      });

      it('should mark the input as required', () => {
        host.required.set(true);
        fixture.detectChanges();

        expect(input().required).toBe(true);
      });

      it('should show the placeholder until a name is known', () => {
        const label = () => fixture.nativeElement.querySelector('mat-label').textContent.trim();
        expect(label()).toBe('Owner');

        host.control.setValue('user-1-1');
        fixture.detectChanges();
        expect(label()).toBe('Max Mustermann');
      });
    });
  });

  describe('without a form control', () => {
    let fixture: ComponentFixture<StandaloneHostComponent>;
    let typeAhead: TypeAheadComponent;

    beforeEach(() => {
      fixture = TestBed.createComponent(StandaloneHostComponent);
      fixture.detectChanges();
      typeAhead = fixture.debugElement.query(By.directive(TypeAheadComponent)).componentInstance;
    });

    const typeAndSettle = (value: string) => {
      typeAhead.inputControl.setValue(value);
      vi.advanceTimersByTime(DEBOUNCE_MS);
      fixture.detectChanges();
    };

    it('should emit the selected access id', () => {
      typeAndSettle('user-1-1');

      expect(fixture.componentInstance.selected).toEqual([null, users[0]]);
    });

    it('should display an error from the lookup result when no exact match is found', () => {
      typeAndSettle('unknown');

      expect(typeAhead.hasError()).toBe(true);
      expect(fixture.nativeElement.querySelector('mat-error').textContent.trim()).toBe('Access id not valid');
    });

    it('should display an error when the lookup failed', () => {
      searchForAccessId.mockReturnValueOnce(throwError(() => new Error('lookup failed')) as Observable<AccessId[]>);
      typeAndSettle('user-1-1');

      expect(typeAhead.hasError()).toBe(true);
    });
  });
});
