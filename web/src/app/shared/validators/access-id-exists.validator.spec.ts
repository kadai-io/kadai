import { describe, it, expect, vi, beforeEach } from 'vitest';
import { FormControl } from '@angular/forms';
import { of, throwError, firstValueFrom } from 'rxjs';
import { accessIdExistsValidator } from './access-id-exists.validator';
import { AccessIdsService } from 'app/shared/services/access-ids/access-ids.service';

describe('accessIdExistsValidator', () => {
  let mockAccessIdsService: { searchForAccessId: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockAccessIdsService = {
      searchForAccessId: vi.fn()
    };
  });

  it('should return null if control value is empty', async () => {
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toBeNull();
    expect(mockAccessIdsService.searchForAccessId).not.toHaveBeenCalled();
  });

  it('should validate successfully when accessId exists in service response', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'user123' }, { accessId: 'admin' }]));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('user123');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toBeNull();
    expect(mockAccessIdsService.searchForAccessId).toHaveBeenCalledWith('user123');
  });

  it('should set invalidAccessId error when accessId is not found', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'otherUser' }]));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('nonExistingUser');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toEqual({ invalidAccessId: true });
  });

  it('should correctly handle object values with accessId property', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'john_doe' }]));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl({ accessId: 'john_doe' } as any);

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toBeNull();
    expect(mockAccessIdsService.searchForAccessId).toHaveBeenCalledWith('john_doe');
  });

  it('should ignore case sensitivity when matching accessId', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(of([{ accessId: 'ADMIN' }]));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('admin');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toBeNull();
  });

  it('should return accessIdLookupError on HTTP error', async () => {
    mockAccessIdsService.searchForAccessId.mockReturnValue(throwError(() => new Error('Server error')));
    const validator = accessIdExistsValidator(mockAccessIdsService as unknown as AccessIdsService);
    const control = new FormControl('someUser');

    const result = await firstValueFrom(validator(control) as any);

    expect(result).toEqual({ accessIdLookupError: true });
  });
});
