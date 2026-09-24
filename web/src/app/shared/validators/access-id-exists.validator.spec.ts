import { describe, it, expect, vi, beforeEach } from 'vitest';
import { FormControl } from '@angular/forms';
import { of, throwError, firstValueFrom } from 'rxjs';
import { accessIdExistsValidator } from './access-id-exists.validator';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';

describe('accessIdExistsValidator', () => {
  let mockAccessIdsService: { validateAccessId: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockAccessIdsService = {
      validateAccessId: vi.fn()
    };
  });

  it('should return null if control value is empty', async () => {
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toBeNull();
    expect(mockAccessIdsService.validateAccessId).not.toHaveBeenCalled();
  });

  it('should validate successfully when validateAccessId returns true', async () => {
    mockAccessIdsService.validateAccessId.mockReturnValue(of(true));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('user123');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toBeNull();
    expect(mockAccessIdsService.validateAccessId).toHaveBeenCalledWith('user123');
  });

  it('should set invalidAccessId error when validateAccessId returns false', async () => {
    mockAccessIdsService.validateAccessId.mockReturnValue(of(false));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('nonExistingUser');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toEqual({ invalidAccessId: true });
    expect(mockAccessIdsService.validateAccessId).toHaveBeenCalledWith('nonExistingUser');
  });

  it('should correctly handle object values with accessId property', async () => {
    mockAccessIdsService.validateAccessId.mockReturnValue(of(true));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl({ accessId: 'john_doe' } as any);

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toBeNull();
    expect(mockAccessIdsService.validateAccessId).toHaveBeenCalledWith('john_doe');
  });

  it('should return accessIdLookupError on HTTP error', async () => {
    mockAccessIdsService.validateAccessId.mockReturnValue(throwError(() => new Error('Server error')));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('someUser');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toEqual({ accessIdLookupError: true });
    expect(mockAccessIdsService.validateAccessId).toHaveBeenCalledWith('someUser');
  });
});
