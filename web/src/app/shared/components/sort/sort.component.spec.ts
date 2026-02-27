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
import { beforeEach, describe, expect, it } from 'vitest';
import { SortComponent } from './sort.component';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Direction, Sorting } from 'app/shared/models/sorting';

describe('SortComponent', () => {
  let component: SortComponent<string>;
  let fixture: ComponentFixture<SortComponent<string>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SortComponent],
      providers: [provideNoopAnimations()]
    }).compileComponents();

    fixture = TestBed.createComponent(SortComponent<string>);
    component = fixture.componentInstance;

    component.sortingFields = new Map<string, string>([
      ['name', 'Name'],
      ['priority', 'Priority']
    ]);
    component.defaultSortBy = 'name';
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('ngOnInit should set sort["sort-by"] to defaultSortBy', () => {
    fixture.detectChanges();
    expect(component.sort['sort-by']).toBe('name');
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

  it('sort should start with Direction.ASC by default', () => {
    fixture.detectChanges();
    expect(component.sort.order).toBe(Direction.ASC);
  });

  it('sortDirectionEnum should expose Direction enum', () => {
    fixture.detectChanges();
    expect(component.sortDirectionEnum).toBe(Direction);
  });
});
