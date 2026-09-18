import { describe, it, expect } from 'vitest';
import { FormControl, FormGroup } from '@angular/forms';
import { permissionDependencyValidator } from './permission-dependency.validator';

describe('permissionDependencyValidator (Vitest)', () => {
  it('should return null if control value is null or undefined', () => {
    const control = new FormControl(null, permissionDependencyValidator);
    expect(control.errors).toBeNull();
    expect(control.valid).toBeTruthy();
  });

  it('should return null when all permissions are granted correctly', () => {
    const validPermissions = {
      permEditTasks: true,
      permReadTasks: true,
      permRead: true,
      permOpen: true,
      permDistribute: true,
      permAppend: true,
      permTransfer: true
    };

    const control = new FormControl(validPermissions, permissionDependencyValidator);
    expect(control.errors).toBeNull();
    expect(control.valid).toBeTruthy();
  });

  it('should return warning when permEditTasks is true but permReadTasks or permRead is missing', () => {
    const invalidPermissions = {
      permEditTasks: true,
      permReadTasks: false,
      permRead: true
    };

    const control = new FormControl(invalidPermissions, permissionDependencyValidator);

    expect(control.invalid).toBeTruthy();
    expect(control.errors).toEqual({
      permissionWarnings: ['PERM_EDIT_TASKS_MISSING_DEPENDING_PERMISSION']
    });
  });

  it('should return warning when permReadTasks is true but permRead is missing', () => {
    const invalidPermissions = {
      permReadTasks: true,
      permRead: false,
      permOpen: false
    };

    const control = new FormControl(invalidPermissions, permissionDependencyValidator);

    expect(control.invalid).toBeTruthy();
    expect(control.errors).toEqual({
      permissionWarnings: ['PERM_READ_TASKS_MISSING_DEPENDING_PERMISSIONS']
    });
  });

  it('should return warning when permOpen is true but permReadTasks and permRead is missing', () => {
    const invalidPermissions = {
      permOpen: true,
      permReadTasks: false,
      permRead: false
    };

    const control = new FormControl(invalidPermissions, permissionDependencyValidator);

    expect(control.invalid).toBeTruthy();
    expect(control.errors).toEqual({
      permissionWarnings: ['PERM_OPEN_MISSING_DEPENDING_PERMISSIONS']
    });
  });

  it('should return warning when permDistribute is true but permAppend or permTransfer is missing', () => {
    const invalidPermissions = {
      permDistribute: true,
      permAppend: true,
      permTransfer: false
    };

    const control = new FormControl(invalidPermissions, permissionDependencyValidator);

    expect(control.invalid).toBeTruthy();
    expect(control.errors).toEqual({
      permissionWarnings: ['PERM_DISTRIBUTE_MISSING_DEPENDING_PERMISSIONS']
    });
  });

  it('should return multiple warnings if several dependency rules are violated simultaneously', () => {
    const invalidPermissions = {
      permEditTasks: true,
      permReadTasks: false,
      permRead: false,
      permDistribute: true,
      permAppend: false,
      permTransfer: false
    };

    const control = new FormControl(invalidPermissions, permissionDependencyValidator);

    expect(control.invalid).toBeTruthy();
    expect(control.errors).toEqual({
      permissionWarnings: [
        'PERM_EDIT_TASKS_MISSING_DEPENDING_PERMISSION',
        'PERM_DISTRIBUTE_MISSING_DEPENDING_PERMISSIONS'
      ]
    });
  });

  it('should work correctly when applied to a FormGroup directly', () => {
    const formGroup = new FormGroup(
      {
        permEditTasks: new FormControl(true),
        permReadTasks: new FormControl(false),
        permRead: new FormControl(true)
      },
      { validators: [permissionDependencyValidator] }
    );

    expect(formGroup.invalid).toBeTruthy();
    expect(formGroup.errors).toEqual({
      permissionWarnings: ['PERM_EDIT_TASKS_MISSING_DEPENDING_PERMISSION']
    });
  });
});
