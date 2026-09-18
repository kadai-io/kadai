import { describe, it, expect, beforeEach, vi } from 'vitest';
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

  const waitForValidation = (control: FormControl) => {
    if (control.status !== 'PENDING') {
      return Promise.resolve(control.status);
    }
    return firstValueFrom(control.statusChanges.pipe(filter((status) => status !== 'PENDING')));
  };

  it('should trigger async validation on control when directive is applied', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'user123' }]));

    hostComponent.control.setValue('user123');
    await waitForValidation(hostComponent.control);

    expect(hostComponent.control.valid).toBeTruthy();
    expect(mockAccessIdsService.searchForAccessId).toHaveBeenCalledWith('user123');
  });
});
