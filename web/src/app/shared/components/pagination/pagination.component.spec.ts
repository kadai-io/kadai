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
import { PaginationComponent } from './pagination.component';
import { DebugElement } from '@angular/core';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Subject } from 'rxjs';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

describe('PaginationComponent', () => {
  let fixture: ComponentFixture<PaginationComponent>;
  let debugElement: DebugElement;
  let component: PaginationComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginationComponent],
      providers: [provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(PaginationComponent);
    debugElement = fixture.debugElement;
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.page = { totalPages: 10 };
    component.pageNumbers = [];
    for (let i = 1; i <= component.page.totalPages; i++) {
      component.pageNumbers.push(i);
    }
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should suggest page 3 when filter() is called with 3', () => {
    component.filter(3);
    expect(component.filteredPages).toEqual(['3']);
  });

  it('should suggest all pages when input of filter() is out of range', () => {
    component.filter(11);
    expect(component.filteredPages).toEqual(component.pageNumbers.map(String));
    component.filter(-1);
    expect(component.filteredPages).toEqual(component.pageNumbers.map(String));
  });

  it('should suggest all pages when input of filter() is not a number', () => {
    component.filter('abc');
    expect(component.filteredPages).toEqual(component.pageNumbers.map(String));
    component.filter('');
    expect(component.filteredPages).toEqual(component.pageNumbers.map(String));
  });

  it('changeToPage should increment pageSelected when going forward', () => {
    component.pageSelected = 2;
    component.changeToPage({ pageIndex: 2, previousPageIndex: 1 });
    expect(component.pageSelected).toBe(3);
  });

  it('changeToPage should decrement pageSelected when going backward', () => {
    component.pageSelected = 3;
    component.changeToPage({ pageIndex: 1, previousPageIndex: 2 });
    expect(component.pageSelected).toBe(2);
  });

  it('changeToPage should emit the new page (1-indexed)', () => {
    const emitSpy = vi.spyOn(component.changePage, 'emit');
    component.changeToPage({ pageIndex: 4, previousPageIndex: 3 });
    expect(emitSpy).toHaveBeenCalledWith(5);
  });

  it('goToPage should set paginator pageIndex and emit page', () => {
    const emitSpy = vi.spyOn(component.changePage, 'emit');
    component.goToPage(5);
    expect(component.pageSelected).toBe(5);
    expect(emitSpy).toHaveBeenCalledWith(5);
  });

  it('updateGoto should populate pageNumbers based on page.totalPages', () => {
    component.page = { totalPages: 3 } as any;
    component.updateGoto();
    expect(component.pageNumbers).toEqual([1, 2, 3]);
  });

  it('changeLabel getRangeLabel should return "loading..." when length is 0', () => {
    component.changeLabel();
    const result = component.paginator._intl.getRangeLabel(0, 10, 0);
    expect(result).toBe('loading...');
  });

  it('changeLabel getRangeLabel should return correct range when length > 0', () => {
    component.changeLabel();
    const result = component.paginator._intl.getRangeLabel(0, 10, 21);
    expect(result).toBe('1 - 10 of 21');
  });

  it('should subscribe to resetPaging and call goToPage(1) when triggered', () => {
    const resetPaging$ = new Subject<null>();
    component.resetPaging = resetPaging$.asObservable();
    component.ngOnInit();
    const goToPageSpy = vi.spyOn(component, 'goToPage');
    resetPaging$.next(null);
    expect(goToPageSpy).toHaveBeenCalledWith(1);
  });

  it('hasItems should be false when numberOfItems is 0', () => {
    component.numberOfItems = 0;
    component.ngOnChanges({ numberOfItems: { currentValue: 0 } } as any);
    expect(component.hasItems).toBe(false);
  });

  it('hasItems should be true when numberOfItems > 0', () => {
    component.numberOfItems = 5;
    component.ngOnChanges({ numberOfItems: { currentValue: 5 } } as any);
    expect(component.hasItems).toBe(true);
  });

  it('ngOnChanges should update pageSelected from page.number', () => {
    component.ngOnChanges({ page: { currentValue: { number: 3, totalPages: 10 } } } as any);
    expect(component.pageSelected).toBe(3);
  });

  it('should not render go-to page section when expanded is false', () => {
    component.expanded = false;
    fixture.detectChanges();
    const goTo = fixture.nativeElement.querySelector('.pagination__go-to');
    expect(goTo).toBeNull();
  });

  it('should render go-to page section when expanded is true', () => {
    component.expanded = true;
    fixture.detectChanges();
    const goTo = fixture.nativeElement.querySelector('.pagination__go-to');
    expect(goTo).toBeTruthy();
  });

  it('should call onSelectText when input is clicked', () => {
    component.expanded = true;
    fixture.detectChanges();
    const onSelectTextSpy = vi.spyOn(component, 'onSelectText');
    const input = fixture.nativeElement.querySelector('#inputTypeAhead') as HTMLInputElement;
    expect(input).toBeTruthy();
    input.click();
    fixture.detectChanges();
    expect(onSelectTextSpy).toHaveBeenCalled();
  });

  it('should call filter when input receives focus', () => {
    component.expanded = true;
    component.page = { totalPages: 5 } as any;
    component.updateGoto();
    fixture.detectChanges();
    const filterSpy = vi.spyOn(component, 'filter');
    const input = fixture.nativeElement.querySelector('#inputTypeAhead') as HTMLInputElement;
    expect(input).toBeTruthy();
    input.dispatchEvent(new Event('focus'));
    fixture.detectChanges();
    expect(filterSpy).toHaveBeenCalledWith(component.pageSelected);
  });

  it('should call filter via ngModelChange when input value changes', () => {
    component.expanded = true;
    component.page = { totalPages: 5 } as any;
    component.updateGoto();
    fixture.detectChanges();
    const filterSpy = vi.spyOn(component, 'filter');
    const input = fixture.nativeElement.querySelector('#inputTypeAhead') as HTMLInputElement;
    expect(input).toBeTruthy();
    input.value = '3';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(filterSpy).toHaveBeenCalled();
  });

  it('should call changeToPage when mat-paginator emits a page event', () => {
    component.page = { totalPages: 10, totalElements: 100, size: 10 } as any;
    fixture.detectChanges();
    const changeToPageSpy = vi.spyOn(component, 'changeToPage');
    component.paginator.page.emit({ pageIndex: 1, previousPageIndex: 0, pageSize: 10, length: 100 });
    fixture.detectChanges();
    expect(changeToPageSpy).toHaveBeenCalledWith(expect.objectContaining({ pageIndex: 1, previousPageIndex: 0 }));
  });

  it('should populate pageNumbers with all pages when updateGoto is called', () => {
    component.expanded = true;
    component.page = { totalPages: 3 } as any;
    component.updateGoto();
    expect(component.pageNumbers).toHaveLength(3);
    expect(component.pageNumbers).toEqual([1, 2, 3]);
    fixture.detectChanges();
    const input = fixture.nativeElement.querySelector('#inputTypeAhead');
    expect(input).toBeTruthy();
  });

  it('should call goToPage when an autocomplete option is selected', () => {
    component.expanded = true;
    component.page = { totalPages: 5 } as any;
    component.updateGoto();
    component.filteredPages = component.pageNumbers.map(String);
    fixture.detectChanges();
    const goToPageSpy = vi.spyOn(component, 'goToPage');
    component.goToPage(3);
    expect(goToPageSpy).toHaveBeenCalledWith(3);
    expect(component.pageSelected).toBe(3);
  });
});
