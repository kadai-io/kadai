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

import { DestroyRef, Directive, ElementRef, inject, Renderer2 } from '@angular/core';

// Resizable cells reserve this much padding on their right, so the handle never covers content.
export const RESIZE_HANDLE_WIDTH_PX = 6;

@Directive({
  selector: '[kadaiResizableWidth]',
  host: {
    '(mousemove)': 'onMousemove($event)',
    '(mouseleave)': 'onMouseleave()',
    '(mousedown)': 'onMousedown($event)'
  }
})
export class ResizableWidthDirective {
  private renderer = inject(Renderer2);
  private el = inject(ElementRef);
  private destroyRef = inject(DestroyRef);

  private startX = 0;
  private handleCursorShown = false;
  // Attached only while dragging: a permanent listener per row would run on every mouse move.
  private stopListening: (() => void) | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => this.stopResizing());
  }

  onMousemove(event: MouseEvent) {
    if (!this.isResizing()) {
      this.showHandleCursor(this.isOverHandle(event));
    }
  }

  onMouseleave() {
    if (!this.isResizing()) {
      this.showHandleCursor(false);
    }
  }

  onMousedown(event: MouseEvent) {
    if (!this.isOverHandle(event)) {
      return;
    }
    this.startX = event.pageX;
    this.showHandleCursor(true);
    this.startResizing();
  }

  private isOverHandle(event: MouseEvent): boolean {
    const distanceFromRightEdge = this.el.nativeElement.getBoundingClientRect().right - event.clientX;
    return distanceFromRightEdge >= 0 && distanceFromRightEdge <= RESIZE_HANDLE_WIDTH_PX;
  }

  private showHandleCursor(show: boolean): void {
    if (show === this.handleCursorShown) {
      return;
    }
    this.handleCursorShown = show;
    this.renderer.setStyle(document.body, 'cursor', show ? 'col-resize' : '');
    this.renderer.setStyle(this.el.nativeElement, 'user-select', show ? 'none' : '');
  }

  private isResizing(): boolean {
    return this.stopListening !== null;
  }

  private startResizing(): void {
    if (this.isResizing()) {
      return;
    }
    const stopMousemove = this.renderer.listen('document', 'mousemove', (event: MouseEvent) => this.resize(event));
    const stopMouseup = this.renderer.listen('document', 'mouseup', () => this.stopResizing());
    this.stopListening = () => {
      stopMousemove();
      stopMouseup();
    };
  }

  private stopResizing(): void {
    this.stopListening?.();
    this.stopListening = null;
    this.showHandleCursor(false);
  }

  private resize(event: MouseEvent): void {
    const target = this.resizeTarget();
    const movementX = event.pageX - this.startX;
    const newWidth = target.clientWidth + movementX;

    this.renderer.setStyle(target, 'min-width', `${newWidth}px`);
    this.renderer.setStyle(target, 'width', `${newWidth}px`);

    this.startX = event.pageX;
  }

  // All cells resize the column's header cell, or one row's width would block the column from shrinking.
  private resizeTarget(): HTMLElement {
    const host: HTMLElement = this.el.nativeElement;
    if (host instanceof HTMLTableCellElement) {
      const headerCell = host.closest('table')?.tHead?.rows[0]?.cells[host.cellIndex];
      if (headerCell) {
        return headerCell;
      }
    }
    return host;
  }
}
