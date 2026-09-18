import { AbstractControl } from '@angular/forms';

export function getPermissionWarnings(control: AbstractControl): string[] {
  const value = control.value;
  if (!value) {
    return [];
  }

  const { permEditTasks, permReadTasks, permRead, permOpen, permDistribute, permAppend, permTransfer } = value;
  const warnings: string[] = [];

  if (permEditTasks && (!permReadTasks || !permRead)) {
    warnings.push('PERM_EDIT_TASKS_MISSING_DEPENDING_PERMISSION');
  }

  if (permReadTasks && !permRead) {
    warnings.push('PERM_READ_TASKS_MISSING_DEPENDING_PERMISSIONS');
  }

  if (permOpen && (!permReadTasks || !permRead)) {
    warnings.push('PERM_OPEN_MISSING_DEPENDING_PERMISSIONS');
  }

  if (permDistribute && (!permAppend || !permTransfer)) {
    warnings.push('PERM_DISTRIBUTE_MISSING_DEPENDING_PERMISSIONS');
  }

  return warnings;
}
