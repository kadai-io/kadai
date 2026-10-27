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

export const RESIZE_HANDLE_WIDTH_PX = 6;

const RESIZABLE_CELL = '[kadaiResizableWidth]';

@Directive({
  selector: 'table[kadaiResizableWidth]',
  host: {
    '(mousedown)': 'onMousedown($event)'
  }
})
export class ResizableWidthDirective {
  private renderer = inject(Renderer2);
  private table: ElementRef<HTMLTableElement> = inject(ElementRef);
  private destroyRef = inject(DestroyRef);

  private startX = 0;
  private target: HTMLElement | null = null;
  private stopListening: (() => void) | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => this.stopResizing());
  }

  onMousedown(event: MouseEvent) {
    const cell = this.handleCellAt(event);
    if (!cell) {
      return;
    }
    this.target = this.headerCellOf(cell);
    this.startX = event.pageX;
    this.renderer.setStyle(document.body, 'user-select', 'none');
    this.startResizing();
  }

  private handleCellAt(event: MouseEvent): HTMLTableCellElement | null {
    if (!(event.target instanceof HTMLElement)) {
      return null;
    }
    const cell = event.target.closest(RESIZABLE_CELL);
    if (!(cell instanceof HTMLTableCellElement) || !this.table.nativeElement.contains(cell)) {
      return null;
    }
    const distanceFromRightEdge = cell.getBoundingClientRect().right - event.clientX;
    return distanceFromRightEdge >= 0 && distanceFromRightEdge <= RESIZE_HANDLE_WIDTH_PX ? cell : null;
  }

  private headerCellOf(cell: HTMLTableCellElement): HTMLElement {
    return this.table.nativeElement.tHead?.rows[0]?.cells[cell.cellIndex] ?? cell;
  }

  private startResizing(): void {
    if (this.stopListening) {
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
    this.target = null;
    this.renderer.setStyle(document.body, 'user-select', '');
  }

  private resize(event: MouseEvent): void {
    if (!this.target) {
      return;
    }
    const movementX = event.pageX - this.startX;
    const newWidth = this.target.clientWidth + movementX;

    this.renderer.setStyle(this.target, 'min-width', `${newWidth}px`);
    this.renderer.setStyle(this.target, 'width', `${newWidth}px`);

    this.startX = event.pageX;
  }
}
