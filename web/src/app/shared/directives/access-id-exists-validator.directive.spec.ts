import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { of, firstValueFrom, filter } from 'rxjs';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';
import { AccessIdExistsValidatorDirective } from './access-id-exists-validator.directive';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, AccessIdExistsValidatorDirective],
  template: `<input [formControl]="control" kadaiAccessIdExists />`
})
class TestHostComponent {
  readonly control = new FormControl('');
}

describe('AccessIdExistsValidatorDirective', () => {
  let hostComponent: TestHostComponent;
  let mockAccessIdsService: { validateAccessId: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    vi.useFakeTimers();

    mockAccessIdsService = {
      validateAccessId: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [{ provide: AccessIdsService, useValue: mockAccessIdsService }]
    }).compileComponents();

    const fixture = TestBed.createComponent(TestHostComponent);
    hostComponent = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  const waitForValidation = async (control: FormControl) => {
    vi.advanceTimersByTime(500);

    if (control.status !== 'PENDING') {
      return Promise.resolve(control.status);
    }
    return firstValueFrom(control.statusChanges.pipe(filter((status) => status !== 'PENDING')));
  };

  it('should trigger async validation on control when directive is applied', async () => {
    mockAccessIdsService.validateAccessId.mockReturnValue(of(true));

    hostComponent.control.setValue('user123');
    await waitForValidation(hostComponent.control);

    expect(hostComponent.control.valid).toBeTruthy();
    expect(mockAccessIdsService.validateAccessId).toHaveBeenCalledWith('user123');
  });

  it('should set invalid status when validateAccessId returns false', async () => {
    mockAccessIdsService.validateAccessId.mockReturnValue(of(false));

    hostComponent.control.setValue('invalidUser');
    await waitForValidation(hostComponent.control);

    expect(hostComponent.control.invalid).toBeTruthy();
    expect(hostComponent.control.hasError('invalidAccessId')).toBeTruthy();
    expect(mockAccessIdsService.validateAccessId).toHaveBeenCalledWith('invalidUser');
  });
});
