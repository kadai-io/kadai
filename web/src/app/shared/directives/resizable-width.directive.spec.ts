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

import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { RESIZE_HANDLE_WIDTH_PX, ResizableWidthDirective } from './resizable-width.directive';

const DRAG_EVENT_TYPES = ['mousemove', 'mouseup'];

type ListenerCall = [type: string, ...rest: unknown[]];

const dragEventTypes = (calls: ListenerCall[]): string[] =>
  calls.map(([type]) => type).filter((type) => DRAG_EVENT_TYPES.includes(type));

const stubRightEdge = (element: HTMLElement, right: number) =>
  vi.spyOn(element, 'getBoundingClientRect').mockReturnValue(new DOMRect(0, 0, right, 0));

const stubClientWidth = (element: HTMLElement) =>
  vi.spyOn(element, 'clientWidth', 'get').mockImplementation(() => parseFloat(element.style.width) || 0);

const mousedownAt = (element: HTMLElement, pageX: number) => {
  const mousedown = new MouseEvent('mousedown', { bubbles: true, clientX: pageX });
  Object.defineProperty(mousedown, 'pageX', { value: pageX });
  element.dispatchEvent(mousedown);
};

@Component({
  imports: [ResizableWidthDirective],
  template: `
    <table kadaiResizableWidth>
      <thead>
        <tr>
          <th></th>
          <th id="header" kadaiResizableWidth>AccessID</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td></td>
          <td id="first-row" kadaiResizableWidth><input id="content" /></td>
        </tr>
        <tr>
          <td></td>
          <td id="second-row" kadaiResizableWidth></td>
        </tr>
        <tr>
          <td></td>
          <td id="plain"></td>
        </tr>
      </tbody>
    </table>
  `
})
class TableTestComponent {}

describe('ResizableWidthDirective', () => {
  let fixture: ComponentFixture<TableTestComponent>;

  const cell = (id: string): HTMLElement => fixture.nativeElement.querySelector(`#${id}`);

  const dragFrom = (id: string, from: number, to: number) => {
    stubRightEdge(cell(id), from);
    mousedownAt(cell(id), from);
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

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should resize the column through its header cell when a body cell is dragged', () => {
    stubClientWidth(cell('header'));
    cell('header').style.width = '80px';

    dragFrom('first-row', 100, 220);

    expect(cell('header').style.width).toBe('200px');
    expect(cell('header').style.minWidth).toBe('200px');
    expect(cell('first-row').style.width).toBe('');
  });

  it('should resize the column when its header cell is dragged', () => {
    stubClientWidth(cell('header'));
    cell('header').style.width = '80px';

    dragFrom('header', 100, 220);

    expect(cell('header').style.width).toBe('200px');
  });

  it('should let every row of the column widen the same column', () => {
    stubClientWidth(cell('header'));

    dragFrom('first-row', 100, 220);
    expect(cell('header').style.width).toBe('120px');

    dragFrom('second-row', 0, 50);

    expect(cell('header').style.width).toBe('170px');
    expect(cell('header').style.minWidth).toBe('170px');
    expect(cell('second-row').style.width).toBe('');
  });

  it('should not start a drag away from the right edge of a cell', () => {
    const addEventListener = vi.spyOn(document, 'addEventListener');
    stubRightEdge(cell('first-row'), 200);

    mousedownAt(fixture.nativeElement.querySelector('#content'), 150);

    expect(dragEventTypes(addEventListener.mock.calls)).toEqual([]);
    expect(cell('header').style.width).toBe('');
  });

  it('should start a drag at the handle width', () => {
    const addEventListener = vi.spyOn(document, 'addEventListener');
    stubRightEdge(cell('first-row'), 200);

    mousedownAt(cell('first-row'), 200 - RESIZE_HANDLE_WIDTH_PX);

    expect(dragEventTypes(addEventListener.mock.calls)).toEqual(['mousemove', 'mouseup']);
  });

  it('should not start a drag just outside the handle width', () => {
    const addEventListener = vi.spyOn(document, 'addEventListener');
    stubRightEdge(cell('first-row'), 200);

    mousedownAt(cell('first-row'), 200 - RESIZE_HANDLE_WIDTH_PX - 1);

    expect(dragEventTypes(addEventListener.mock.calls)).toEqual([]);
  });

  it('should ignore a mousedown outside a resizable cell', () => {
    const addEventListener = vi.spyOn(document, 'addEventListener');
    stubRightEdge(cell('plain'), 200);

    mousedownAt(cell('plain'), 200);

    expect(dragEventTypes(addEventListener.mock.calls)).toEqual([]);
  });

  it('should listen on the document only while dragging', () => {
    const addEventListener = vi.spyOn(document, 'addEventListener');
    const removeEventListener = vi.spyOn(document, 'removeEventListener');

    expect(dragEventTypes(addEventListener.mock.calls)).toEqual([]);

    stubRightEdge(cell('first-row'), 100);
    mousedownAt(cell('first-row'), 100);
    expect(dragEventTypes(addEventListener.mock.calls)).toEqual(['mousemove', 'mouseup']);

    document.dispatchEvent(new MouseEvent('mouseup'));
    expect(dragEventTypes(removeEventListener.mock.calls)).toEqual(['mousemove', 'mouseup']);
  });

  it('should stop listening on the document when the table is destroyed while dragging', () => {
    const removeEventListener = vi.spyOn(document, 'removeEventListener');

    stubRightEdge(cell('first-row'), 100);
    mousedownAt(cell('first-row'), 100);
    fixture.destroy();

    expect(dragEventTypes(removeEventListener.mock.calls)).toEqual(['mousemove', 'mouseup']);
  });

  it('should prevent text selection while dragging', () => {
    stubRightEdge(cell('first-row'), 100);
    mousedownAt(cell('first-row'), 100);
    expect(document.body.style.userSelect).toBe('none');

    document.dispatchEvent(new MouseEvent('mouseup'));
    expect(document.body.style.userSelect).toBe('');
  });
});
