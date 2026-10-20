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

import { Component, DebugElement, runInInjectionContext } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ResizableWidthDirective } from './resizable-width.directive';
import { beforeEach, describe, expect, it } from 'vitest';

@Component({
  imports: [ResizableWidthDirective],
  template: ` <div kadaiResizableWidth></div>`
})
class TestComponent {}

describe('ResizableDirective', () => {
  let fixture: ComponentFixture<TestComponent>;
  let inputDebug: DebugElement;
  let inputElement: HTMLInputElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TestComponent]
    });
    fixture = TestBed.createComponent(TestComponent);
    fixture.detectChanges();

    inputDebug = fixture.debugElement.query(By.directive(ResizableWidthDirective));
    inputElement = inputDebug.nativeElement;
  });

  it('should create an instance', () => {
    let directive!: ResizableWidthDirective;
    runInInjectionContext(fixture.componentRef.injector, () => {
      directive = new ResizableWidthDirective();
    });
    expect(directive).toBeTruthy();
  });

  it('should handle mouseover', () => {
    inputElement.dispatchEvent(new MouseEvent('mouseover'));

    expect(document.body.style.cursor).toBe('col-resize');
    expect(inputElement.style.userSelect).toBe('none');
  });

  it('should handle mouseout', () => {
    inputElement.dispatchEvent(new MouseEvent('mouseout'));

    expect(document.body.style.cursor).toBe('');
    expect(inputElement.style.userSelect).toBe('');
  });

  it('should resize element after mousedown and mousemove', () => {
    const startPosition: number = 100;
    const endPosition: number = 200;

    const mousedownEvent = new MouseEvent('mousedown', { bubbles: true, cancelable: true });
    Object.defineProperty(mousedownEvent, 'pageX', { value: startPosition });
    inputElement.dispatchEvent(mousedownEvent);

    const initialWidth = inputElement.clientWidth;
    const mousemoveEvent = new MouseEvent('mousemove', { bubbles: true, cancelable: true });
    Object.defineProperty(mousemoveEvent, 'pageX', { value: endPosition });
    inputElement.dispatchEvent(mousemoveEvent);

    const expectedWidth = initialWidth + (endPosition - startPosition);
    expect(inputElement.style.minWidth).toBe(`${expectedWidth}px`);
    expect(inputElement.style.width).toBe(`${expectedWidth}px`);
  });

  it('should not resize element after mouseup', () => {
    const startPosition: number = 100;
    const endPosition: number = 200;

    const mouseupEvent = new MouseEvent('mouseup', { bubbles: true, cancelable: true });
    Object.defineProperty(mouseupEvent, 'pageX', { value: startPosition });
    inputElement.dispatchEvent(mouseupEvent);

    fixture.detectChanges();

    const mousemoveEvent = new MouseEvent('mousemove', { bubbles: true, cancelable: true });
    Object.defineProperty(mousemoveEvent, 'pageX', { value: endPosition });
    inputElement.dispatchEvent(mousemoveEvent);

    expect(inputElement.style.minWidth).toBe('');
    expect(inputElement.style.width).toBe('');
  });
});

@Component({
  imports: [ResizableWidthDirective],
  template: `
    <table>
      <thead>
        <tr>
          <th></th>
          <th id="header" kadaiResizableWidth>AccessID</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td></td>
          <td id="first-row" kadaiResizableWidth></td>
        </tr>
        <tr>
          <td></td>
          <td id="second-row" kadaiResizableWidth></td>
        </tr>
      </tbody>
    </table>
  `
})
class TableTestComponent {}

describe('ResizableDirective in a table', () => {
  let fixture: ComponentFixture<TableTestComponent>;
  const cell = (id: string): HTMLElement => fixture.nativeElement.querySelector(`#${id}`);

  const drag = (element: HTMLElement, from: number, to: number) => {
    const mousedown = new MouseEvent('mousedown', { bubbles: true });
    Object.defineProperty(mousedown, 'pageX', { value: from });
    element.dispatchEvent(mousedown);
    const mousemove = new MouseEvent('mousemove', { bubbles: true });
    Object.defineProperty(mousemove, 'pageX', { value: to });
    document.dispatchEvent(mousemove);
    document.dispatchEvent(new MouseEvent('mouseup'));
  };

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [TableTestComponent] });
    fixture = TestBed.createComponent(TableTestComponent);
    fixture.detectChanges();
  });

  it('should resize the column through its header cell when a body cell is dragged', () => {
    drag(cell('first-row'), 100, 220);

    expect(cell('header').style.width).toBe('120px');
    expect(cell('header').style.minWidth).toBe('120px');
    expect(cell('first-row').style.width).toBe('');
  });

  it('should let every row of the column resize the same width', () => {
    drag(cell('first-row'), 100, 220);
    drag(cell('second-row'), 0, 50);

    expect(cell('header').style.width).toBe('50px');
    expect(cell('first-row').style.width).toBe('');
    expect(cell('second-row').style.width).toBe('');
  });
});
