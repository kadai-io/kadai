import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Component, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { of, throwError, firstValueFrom, filter } from 'rxjs';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';
import { AccessIdExistsValidatorDirective } from './access-id-exists-validator.directive';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, AccessIdExistsValidatorDirective],
  template: `<input [formControl]="control" kadaiAccessIdExists />`
})
class TestHostComponent {
  readonly control = new FormControl('');
  readonly directive = viewChild.required(AccessIdExistsValidatorDirective);
}

describe('AccessIdExistsValidatorDirective', () => {
  let hostComponent: TestHostComponent;
  let mockAccessIdsService: { searchForAccessId: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockAccessIdsService = {
      searchForAccessId: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [{ provide: AccessIdsService, useValue: mockAccessIdsService }]
    }).compileComponents();

    const fixture = TestBed.createComponent(TestHostComponent);
    hostComponent = fixture.componentInstance;
    fixture.detectChanges();
  });

  // Вспомогательная функция для ожидания завершения асинхронной валидации контрола
  const waitForValidation = (control: FormControl) => {
    if (control.status !== 'PENDING') {
      return Promise.resolve(control.status);
    }
    return firstValueFrom(control.statusChanges.pipe(filter((status) => status !== 'PENDING')));
  };

  it('should return null if control value is empty', async () => {
    const control = new FormControl('');
    const directive = hostComponent.directive();

    const result = await firstValueFrom(directive.validate(control) as any);

    expect(result).toBeNull();
    expect(mockAccessIdsService.searchForAccessId).not.toHaveBeenCalled();
  });

  it('should validate successfully when accessId exists in service response', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'user123' }, { accessId: 'admin' }]));

    hostComponent.control.setValue('user123');
    await waitForValidation(hostComponent.control);

    expect(hostComponent.control.valid).toBeTruthy();
    expect(hostComponent.control.errors).toBeNull();
    expect(mockAccessIdsService.searchForAccessId).toHaveBeenCalledWith('user123');
  });

  it('should set invalidAccessId error when accessId is not found', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'otherUser' }]));

    hostComponent.control.setValue('nonExistingUser');
    await waitForValidation(hostComponent.control);

    expect(hostComponent.control.invalid).toBeTruthy();
    expect(hostComponent.control.errors).toEqual({ invalidAccessId: true });
  });

  it('should correctly handle object values with accessId property', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'john_doe' }]));

    hostComponent.control.setValue({ accessId: 'john_doe' } as any);
    await waitForValidation(hostComponent.control);

    expect(hostComponent.control.valid).toBeTruthy();
    expect(mockAccessIdsService.searchForAccessId).toHaveBeenCalledWith('john_doe');
  });

  it('should ignore case sensitivity when matching accessId', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'ADMIN' }]));

    hostComponent.control.setValue('admin');
    await waitForValidation(hostComponent.control);

    expect(hostComponent.control.valid).toBeTruthy();
  });

  it('should return accessIdLookupError on HTTP error', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(throwError(() => new Error('Server error')));

    hostComponent.control.setValue('someUser');
    await waitForValidation(hostComponent.control);

    expect(hostComponent.control.invalid).toBeTruthy();
    expect(hostComponent.control.errors).toEqual({ accessIdLookupError: true });
  });
});
