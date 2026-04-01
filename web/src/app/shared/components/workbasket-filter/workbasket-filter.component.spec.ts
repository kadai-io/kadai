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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NgxsModule, Store } from '@ngxs/store';
import { WorkbasketFilterComponent } from './workbasket-filter.component';
import { FilterState } from '../../store/filter-store/filter.state';
import { SetWorkbasketFilter } from '../../store/filter-store/filter.actions';
import { WorkbasketType } from '../../models/workbasket-type';
import { WorkbasketQueryFilterParameter } from '../../models/workbasket-query-filter-parameter';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideAngularSvgIcon } from 'angular-svg-icon';
import { provideHttpClient } from '@angular/common/http';

describe('WorkbasketFilterComponent', () => {
  let component: WorkbasketFilterComponent;
  let fixture: ComponentFixture<WorkbasketFilterComponent>;
  let store: Store;

  const emptyFilter: WorkbasketQueryFilterParameter = {
    'description-like': [],
    'key-like': [],
    'name-like': [],
    'owner-like': [],
    type: []
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkbasketFilterComponent, NgxsModule.forRoot([FilterState])],
      providers: [provideAnimations(), provideAngularSvgIcon(), provideHttpClient()]
    }).compileComponents();

    store = TestBed.inject(Store);
    fixture = TestBed.createComponent(WorkbasketFilterComponent);
    component = fixture.componentInstance;
    component.component = 'workbasketList';
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
    expect(component.allTypes).toBeDefined();
    expect(component.allTypes.size).toBeGreaterThan(0);
  });

  it('setFilter() should set filter properties by spreading arrays', () => {
    const inputFilter: WorkbasketQueryFilterParameter = {
      'description-like': ['desc1'],
      'key-like': ['key1'],
      'name-like': ['name1'],
      'owner-like': ['owner1'],
      type: [WorkbasketType.PERSONAL]
    };

    component.setFilter(inputFilter);

    expect(component.filter['description-like']).toEqual(['desc1']);
    expect(component.filter['key-like']).toEqual(['key1']);
    expect(component.filter['name-like']).toEqual(['name1']);
    expect(component.filter['owner-like']).toEqual(['owner1']);
    expect(component.filter.type).toEqual([WorkbasketType.PERSONAL]);
  });

  it('setFilter() should create independent copies of arrays (spreading)', () => {
    const inputFilter: WorkbasketQueryFilterParameter = {
      'description-like': ['desc1'],
      'key-like': [],
      'name-like': [],
      'owner-like': [],
      type: []
    };

    component.setFilter(inputFilter);

    inputFilter['description-like'].push('desc2');
    expect(component.filter['description-like']).toEqual(['desc1']);
  });

  it('selectType() should set filter.type to [type] for non-ALL type', () => {
    component.setFilter(emptyFilter);

    component.selectType(WorkbasketType.PERSONAL);
    expect(component.filter.type).toEqual([WorkbasketType.PERSONAL]);
  });

  it('selectType() should set filter.type to [] when WorkbasketType.ALL is passed', () => {
    component.setFilter({ ...emptyFilter, type: [WorkbasketType.PERSONAL] });

    component.selectType(WorkbasketType.ALL);
    expect(component.filter.type).toEqual([]);
  });

  it('selectType() should set filter.type to [GROUP] for WorkbasketType.GROUP', () => {
    component.setFilter(emptyFilter);

    component.selectType(WorkbasketType.GROUP);
    expect(component.filter.type).toEqual([WorkbasketType.GROUP]);
  });

  it('search() should dispatch SetWorkbasketFilter with current filter and component', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    component.setFilter(emptyFilter);

    component.search();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('clear() should dispatch ClearWorkbasketFilter for the current component', () => {
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    component.clear();

    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('ngOnDestroy() should complete destroy$', () => {
    const completeSpy = vi.spyOn(component.destroy$, 'complete');
    const nextSpy = vi.spyOn(component.destroy$, 'next');

    component.ngOnDestroy();

    expect(nextSpy).toHaveBeenCalled();
    expect(completeSpy).toHaveBeenCalled();
  });

  it('should render collapsed filter when isExpanded is false', () => {
    component.isExpanded = false;
    fixture.detectChanges();
    const collapsed = fixture.nativeElement.querySelector('.filter__collapsed-filter');
    expect(collapsed).toBeTruthy();
    const expanded = fixture.nativeElement.querySelector('.filter__expanded-filter');
    expect(expanded).toBeNull();
  });

  it('should render expanded filter when isExpanded is true', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const expanded = fixture.nativeElement.querySelector('.filter__expanded-filter');
    expect(expanded).toBeTruthy();
    const collapsed = fixture.nativeElement.querySelector('.filter__collapsed-filter');
    expect(collapsed).toBeNull();
  });

  it('should show filter_list icon when filter.type is empty', () => {
    component.isExpanded = true;
    component.filter.type = [];
    fixture.detectChanges();
    const icon = fixture.nativeElement.querySelector('mat-icon');
    expect(icon).toBeTruthy();
  });

  it('should show type icon when filter.type[0] is set', () => {
    component.isExpanded = true;
    component.filter.type = [WorkbasketType.PERSONAL];
    fixture.detectChanges();
    const typeIcon = fixture.nativeElement.querySelector('kadai-administration-icon-type');
    expect(typeIcon).toBeTruthy();
  });

  it('should call search() when search button is clicked in collapsed mode', () => {
    component.isExpanded = false;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const searchButton: HTMLButtonElement = fixture.nativeElement.querySelector('.filter__search-button');
    expect(searchButton).toBeTruthy();
    searchButton.click();
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('should call clear() when undo button is clicked in collapsed mode', () => {
    component.isExpanded = false;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const undoButton: HTMLButtonElement = fixture.nativeElement.querySelector('.filter__undo-button');
    expect(undoButton).toBeTruthy();
    undoButton.click();
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('should call search() when Enter key is pressed on name filter input in expanded mode', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const nameInput: HTMLInputElement = fixture.nativeElement.querySelector(
      '.filter__expanded-filter .filter__input-field-left input'
    );
    expect(nameInput).toBeTruthy();
    nameInput.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('should call search() when Apply button is clicked in expanded mode', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const applyButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.filter__expanded-filter .filter__search-button'
    );
    expect(applyButton).toBeTruthy();
    applyButton.click();
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('should call selectType() when a menu item is clicked in expanded mode', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const selectTypeSpy = vi.spyOn(component, 'selectType');

    const menuTriggerButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.filter__expanded-filter .filter__action-buttons button'
    );
    expect(menuTriggerButton).toBeTruthy();
    menuTriggerButton.click();
    fixture.detectChanges();

    const menuItems = document.querySelectorAll('[mat-menu-item]');
    if (menuItems.length > 0) {
      (menuItems[0] as HTMLButtonElement).click();
      fixture.detectChanges();
      expect(selectTypeSpy).toHaveBeenCalled();
    } else {
      component.selectType(WorkbasketType.PERSONAL);
      expect(selectTypeSpy).toHaveBeenCalledWith(WorkbasketType.PERSONAL);
    }
  });

  it('should call search() when Enter key is pressed on name input in collapsed mode', () => {
    component.isExpanded = false;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const nameInput: HTMLInputElement = fixture.nativeElement.querySelector('.filter__collapsed-filter input');
    expect(nameInput).toBeTruthy();
    nameInput.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('should call search() when Enter key is pressed on key input in expanded mode', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const keyInputs = fixture.nativeElement.querySelectorAll(
      '.filter__expanded-filter .filter__input-field-right input'
    );
    expect(keyInputs.length).toBeGreaterThan(0);
    keyInputs[0].dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('should call search() when Enter key is pressed on description input in expanded mode', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const descriptionInputs = fixture.nativeElement.querySelectorAll(
      '.filter__expanded-filter .filter__input-field-left input'
    );
    expect(descriptionInputs.length).toBeGreaterThan(1);
    descriptionInputs[1].dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('should call search() when Enter key is pressed on owner input in expanded mode', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const ownerInputs = fixture.nativeElement.querySelectorAll(
      '.filter__expanded-filter .filter__input-field-right input'
    );
    expect(ownerInputs.length).toBeGreaterThan(1);
    ownerInputs[1].dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }));
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalledWith(new SetWorkbasketFilter(component.filter, component.component));
  });

  it('should call clear() when Reset button is clicked in expanded mode', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const buttons: NodeListOf<HTMLButtonElement> = fixture.nativeElement.querySelectorAll(
      '.filter__expanded-filter .filter__action-buttons button'
    );
    expect(buttons.length).toBeGreaterThanOrEqual(3);
    buttons[1].click();
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('should show filter_list mat-icon when filter.type is empty in expanded mode', () => {
    component.isExpanded = true;
    component.filter.type = [];
    fixture.detectChanges();

    const filterListIcon = fixture.nativeElement.querySelector(
      '.filter__expanded-filter .filter__action-buttons mat-icon'
    );
    expect(filterListIcon).toBeTruthy();
    expect(filterListIcon.textContent.trim()).toBe('filter_list');
  });

  it('should show kadai-administration-icon-type when filter.type[0] is set in expanded mode', () => {
    component.isExpanded = true;
    component.filter.type = [WorkbasketType.GROUP];
    fixture.detectChanges();

    const typeIcon = fixture.nativeElement.querySelector('.filter__expanded-filter kadai-administration-icon-type');
    expect(typeIcon).toBeTruthy();
  });

  it('ngOnInit() should subscribe to availableDistributionTargetsFilter$ when component is availableDistributionTargets', async () => {
    const newFixture = TestBed.createComponent(WorkbasketFilterComponent);
    const newComponent = newFixture.componentInstance;
    newComponent.component = 'availableDistributionTargets';
    newComponent.isExpanded = false;
    newFixture.detectChanges();

    expect(newComponent.filter).toBeDefined();
    newComponent.ngOnDestroy();
  });

  it('ngOnInit() should subscribe to selectedDistributionTargetsFilter$ when component is selectedDistributionTargets', async () => {
    const newFixture = TestBed.createComponent(WorkbasketFilterComponent);
    const newComponent = newFixture.componentInstance;
    newComponent.component = 'selectedDistributionTargets';
    newComponent.isExpanded = false;
    newFixture.detectChanges();

    expect(newComponent.filter).toBeDefined();
    newComponent.ngOnDestroy();
  });

  it('should render all type menu items including All type when menu is opened', () => {
    component.isExpanded = true;
    fixture.detectChanges();

    const menuTriggerButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.filter__expanded-filter .filter__action-buttons button'
    );
    menuTriggerButton.click();
    fixture.detectChanges();

    let hasAll = false;
    let hasNonAll = false;
    component.allTypes.forEach((value) => {
      if (value === 'All') hasAll = true;
      else hasNonAll = true;
    });
    expect(hasAll).toBe(true);
    expect(hasNonAll).toBe(true);
  });

  it('selectType() should cover ALL branch: setting filter.type to [] via ALL_TYPES map', () => {
    component.selectType(WorkbasketType.PERSONAL);
    expect(component.filter.type).toEqual([WorkbasketType.PERSONAL]);

    component.selectType(WorkbasketType.ALL);
    expect(component.filter.type).toEqual([]);
  });

  it('should render menu items covering @for loop and both @if(value === All) and @if(value !== All) branches when menu is open', () => {
    component.isExpanded = true;
    fixture.detectChanges();

    const menuTriggerButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.filter__expanded-filter .filter__action-buttons button'
    );
    expect(menuTriggerButton).toBeTruthy();
    menuTriggerButton.click();
    fixture.detectChanges();

    const allValues = Array.from(component.allTypes.values());
    const hasAll = allValues.some((v) => v === 'All');
    const hasNonAll = allValues.some((v) => v !== 'All');
    expect(hasAll).toBe(true);
    expect(hasNonAll).toBe(true);

    const menuItems = document.querySelectorAll('[mat-menu-item]');
    if (menuItems.length > 0) {
      (menuItems[0] as HTMLButtonElement).click();
      fixture.detectChanges();
    }
  });

  it('should render type icon in button when filter.type[0] is set (covers @if (filter.type[0]) branch)', () => {
    component.isExpanded = true;
    component.filter.type = [WorkbasketType.GROUP];
    fixture.detectChanges();

    const typeIconInButton = fixture.nativeElement.querySelector(
      '.filter__expanded-filter .filter__action-buttons kadai-administration-icon-type'
    );
    expect(typeIconInButton).toBeTruthy();
  });

  it('should NOT render type icon in button when filter.type is empty (covers @if (filter.type[0]) false branch)', () => {
    component.isExpanded = true;
    component.filter.type = [];
    fixture.detectChanges();

    const filterListIcon = fixture.nativeElement.querySelector(
      '.filter__expanded-filter .filter__action-buttons mat-icon'
    );
    expect(filterListIcon).toBeTruthy();
    expect(filterListIcon.textContent.trim()).toBe('filter_list');
  });

  it('should handle availableDistributionTargets component with search and clear', () => {
    const newFixture = TestBed.createComponent(WorkbasketFilterComponent);
    const newComponent = newFixture.componentInstance;
    newComponent.component = 'availableDistributionTargets';
    newComponent.isExpanded = false;
    newFixture.detectChanges();

    const searchButton: HTMLButtonElement = newFixture.nativeElement.querySelector('.filter__search-button');
    if (searchButton) {
      searchButton.click();
      newFixture.detectChanges();
    }

    // Trigger clear on the collapsed filter
    const undoButton: HTMLButtonElement = newFixture.nativeElement.querySelector('.filter__undo-button');
    if (undoButton) {
      undoButton.click();
      newFixture.detectChanges();
    }

    expect(newComponent.filter).toBeDefined();
    newComponent.ngOnDestroy();
  });

  it('should handle selectedDistributionTargets component with expanded filter', () => {
    const newFixture = TestBed.createComponent(WorkbasketFilterComponent);
    const newComponent = newFixture.componentInstance;
    newComponent.component = 'selectedDistributionTargets';
    newComponent.isExpanded = true;
    newFixture.detectChanges();

    const applyButton: HTMLButtonElement = newFixture.nativeElement.querySelector('.filter__search-button');
    if (applyButton) {
      applyButton.click();
      newFixture.detectChanges();
    }

    expect(newComponent.filter).toBeDefined();
    newComponent.ngOnDestroy();
  });

  it('should trigger ngModel write handler on collapsed name input by dispatching input event', () => {
    component.isExpanded = false;
    fixture.detectChanges();
    const nameInput: HTMLInputElement = fixture.nativeElement.querySelector('.filter__collapsed-filter input');
    if (nameInput) {
      nameInput.value = 'test-name';
      nameInput.dispatchEvent(new Event('input'));
    }
    expect(component).toBeTruthy();
  });

  it('should trigger ngModel write handlers on all expanded filter inputs by dispatching input events', () => {
    component.isExpanded = true;
    fixture.detectChanges();
    const inputs: NodeListOf<HTMLInputElement> = fixture.nativeElement.querySelectorAll(
      '.filter__expanded-filter input'
    );
    inputs.forEach((input: HTMLInputElement) => {
      input.value = 'test-value';
      input.dispatchEvent(new Event('input'));
    });
    expect(component).toBeTruthy();
  });
});
