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
import { SortComponent } from './sort.component';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Direction, Sorting } from 'app/shared/models/sorting';
import { OverlayContainer } from '@angular/cdk/overlay';

describe('SortComponent', () => {
  let component: SortComponent<string>;
  let fixture: ComponentFixture<SortComponent<string>>;
  let overlayContainer: OverlayContainer;
  let overlayContainerElement: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SortComponent],
      providers: [provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(SortComponent<string>);
    component = fixture.componentInstance;

    fixture.componentRef.setInput(
      'sortingFields',
      new Map<string, string>([
        ['name', 'Name'],
        ['priority', 'Priority']
      ])
    );
    fixture.componentRef.setInput('defaultSortBy', 'name');

    overlayContainer = TestBed.inject(OverlayContainer);
    overlayContainerElement = overlayContainer.getContainerElement();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
    expect(component.sort['sort-by']).toBe('name');
    expect(component.sort.order).toBe(Direction.ASC);
    expect(component.sortDirectionEnum).toBe(Direction);
  });

  it('changeOrder should update the sort order and emit via performSorting', () => {
    fixture.detectChanges();

    const emitted: Sorting<string>[] = [];
    component.performSorting.subscribe((val) => emitted.push(val));

    component.changeOrder(Direction.DESC);

    expect(component.sort.order).toBe(Direction.DESC);
    expect(emitted.length).toBe(1);
    expect(emitted[0].order).toBe(Direction.DESC);
  });

  it('changeOrder with ASC should set order to ASC', () => {
    fixture.detectChanges();
    component.sort.order = Direction.DESC;

    const emitted: Sorting<string>[] = [];
    component.performSorting.subscribe((val) => emitted.push(val));

    component.changeOrder(Direction.ASC);

    expect(component.sort.order).toBe(Direction.ASC);
    expect(emitted[0].order).toBe(Direction.ASC);
  });

  it('changeSortBy should update sort["sort-by"] and emit', () => {
    fixture.detectChanges();

    const emitted: Sorting<string>[] = [];
    component.performSorting.subscribe((val) => emitted.push(val));

    component.changeSortBy('priority');

    expect(component.sort['sort-by']).toBe('priority');
    expect(emitted.length).toBe(1);
    expect(emitted[0]['sort-by']).toBe('priority');
  });

  it('should render sort button in the template', () => {
    fixture.detectChanges();
    const sortButton = fixture.nativeElement.querySelector('.sort__button');
    expect(sortButton).toBeTruthy();
  });

  it('should emit performSorting when changeOrder is called with DESC', () => {
    fixture.detectChanges();
    const emitSpy = vi.spyOn(component.performSorting, 'emit');
    component.changeOrder(Direction.DESC);
    expect(emitSpy).toHaveBeenCalledWith(expect.objectContaining({ order: Direction.DESC }));
    expect(component.sort.order).toBe(Direction.DESC);
  });

  it('should emit performSorting when changeSortBy is called with different field', () => {
    fixture.detectChanges();
    const emitSpy = vi.spyOn(component.performSorting, 'emit');
    component.changeSortBy('priority');
    expect(emitSpy).toHaveBeenCalledWith(expect.objectContaining({ 'sort-by': 'priority' }));
    expect(component.sort['sort-by']).toBe('priority');
  });

  it('should initialize with default sort values', () => {
    fixture.detectChanges();
    expect(component.sort['sort-by']).toBe('name');
    expect(component.sort.order).toBe(Direction.ASC);
  });

  it('should call changeOrder(ASC) via DOM click on ascending menu button', () => {
    fixture.detectChanges();
    const changeOrderSpy = vi.spyOn(component, 'changeOrder');

    const sortButton = fixture.nativeElement.querySelector('.sort__button');
    sortButton.click();
    fixture.detectChanges();

    const menuItems = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const sortDirectionItem = Array.from(menuItems).find((el) =>
      el.textContent?.includes('Sort direction')
    ) as HTMLElement;
    sortDirectionItem?.click();
    fixture.detectChanges();

    const allButtons = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const ascButton = Array.from(allButtons).find((el) => el.textContent?.includes('Ascending')) as HTMLElement;
    ascButton?.click();
    fixture.detectChanges();

    expect(changeOrderSpy).toHaveBeenCalledWith(Direction.ASC);
  });

  it('should call changeOrder(DESC) via DOM click on descending menu button', () => {
    fixture.detectChanges();
    const changeOrderSpy = vi.spyOn(component, 'changeOrder');

    const sortButton = fixture.nativeElement.querySelector('.sort__button');
    sortButton.click();
    fixture.detectChanges();

    const menuItems = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const sortDirectionItem = Array.from(menuItems).find((el) =>
      el.textContent?.includes('Sort direction')
    ) as HTMLElement;
    sortDirectionItem?.click();
    fixture.detectChanges();

    const allButtons = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const descButton = Array.from(allButtons).find((el) => el.textContent?.includes('Descending')) as HTMLElement;
    descButton?.click();
    fixture.detectChanges();

    expect(changeOrderSpy).toHaveBeenCalledWith(Direction.DESC);
  });

  it('should call changeSortBy via DOM click on sort value menu button', () => {
    fixture.detectChanges();
    const changeSortBySpy = vi.spyOn(component, 'changeSortBy');

    const sortButton = fixture.nativeElement.querySelector('.sort__button');
    sortButton.click();
    fixture.detectChanges();

    const menuItems = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const sortValueItem = Array.from(menuItems).find((el) => el.textContent?.includes('Sort value')) as HTMLElement;
    sortValueItem?.click();
    fixture.detectChanges();

    const allButtons = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const nameButton = Array.from(allButtons).find((el) => el.textContent?.includes('Name')) as HTMLElement;
    nameButton?.click();
    fixture.detectChanges();

    expect(changeSortBySpy).toHaveBeenCalledWith('name');
  });

  it('should show sort__selected-value class on ASC icon when sort.order is ASC', () => {
    fixture.detectChanges();
    const sortButton = fixture.nativeElement.querySelector('.sort__button');
    sortButton.click();
    fixture.detectChanges();

    const menuItems = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const sortDirectionItem = Array.from(menuItems).find((el) =>
      el.textContent?.includes('Sort direction')
    ) as HTMLElement;
    sortDirectionItem?.click();
    fixture.detectChanges();

    const selectedIcons = overlayContainerElement.querySelectorAll('.sort__selected-value');
    expect(selectedIcons.length).toBeGreaterThan(0);
  });

  it('should show sort__selected-value class on DESC icon when sort.order is DESC', () => {
    const localFixture = TestBed.createComponent(SortComponent);
    const localComponent = localFixture.componentInstance;
    localComponent.sort = { 'sort-by': 'name', order: Direction.DESC };
    localFixture.componentRef.setInput(
      'sortingFields',
      new Map<string, string>([
        ['name', 'Name'],
        ['priority', 'Priority']
      ])
    );
    localFixture.detectChanges();

    const sortButton = localFixture.nativeElement.querySelector('.sort__button');
    sortButton.click();
    localFixture.detectChanges();

    const menuItems = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const sortDirectionItem = Array.from(menuItems).find((el) =>
      el.textContent?.includes('Sort direction')
    ) as HTMLElement;
    sortDirectionItem?.click();
    localFixture.detectChanges();

    const selectedIcons = overlayContainerElement.querySelectorAll('.sort__selected-value');
    expect(selectedIcons.length).toBeGreaterThan(0);
  });

  it('should show sort__selected-value class on active sort field when opened', () => {
    fixture.detectChanges();
    const sortButton = fixture.nativeElement.querySelector('.sort__button');
    sortButton.click();
    fixture.detectChanges();

    const menuItems = overlayContainerElement.querySelectorAll('[mat-menu-item]');
    const sortValueItem = Array.from(menuItems).find((el) => el.textContent?.includes('Sort value')) as HTMLElement;
    sortValueItem?.click();
    fixture.detectChanges();

    const selectedSpans = overlayContainerElement.querySelectorAll('.sort__selected-value');
    expect(selectedSpans.length).toBeGreaterThan(0);
  });

  it('should not show sort__selected-value on inactive sort field', () => {
    const localFixture = TestBed.createComponent(SortComponent);
    const localComponent = localFixture.componentInstance;
    localFixture.componentRef.setInput('defaultSortBy', 'priority');
    localFixture.componentRef.setInput(
      'sortingFields',
      new Map<string, string>([
        ['name', 'Name'],
        ['priority', 'Priority']
      ])
    );
    localFixture.detectChanges();

    expect(localComponent.sort['sort-by']).toBe('priority');
    localComponent.changeSortBy('name');
    expect(localComponent.sort['sort-by']).toBe('name');
  });
});
