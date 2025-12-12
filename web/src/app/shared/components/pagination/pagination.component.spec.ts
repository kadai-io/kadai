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
import { beforeEach, describe, expect, it } from 'vitest';

describe('PaginationComponent', () => {
  let fixture: ComponentFixture<PaginationComponent>;
  let debugElement: DebugElement;
  let component: PaginationComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginationComponent]
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
});
