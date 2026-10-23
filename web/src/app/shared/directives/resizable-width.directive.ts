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

@Directive({
  selector: '[kadaiResizableWidth]',
  host: {
    '(mouseover)': 'onMouseover()',
    '(mouseout)': 'onMouseout()',
    '(mousedown)': 'onMousedown($event)'
  }
})
export class ResizableWidthDirective {
  private renderer = inject(Renderer2);
  private el = inject(ElementRef);
  private destroyRef = inject(DestroyRef);

  private startX = 0;
  // Every cell of the column carries this directive, so permanent document listeners would run one
  // handler per row on every mouse move on the page. They exist only for the duration of a drag.
  private stopListening: (() => void) | null = null;

  constructor() {
    this.destroyRef.onDestroy(() => this.stopResizing());
  }

  onMouseover() {
    this.renderer.setStyle(document.body, 'cursor', 'col-resize');
    this.renderer.setStyle(this.el.nativeElement, 'user-select', 'none');
  }

  onMouseout() {
    this.renderer.setStyle(document.body, 'cursor', '');
    this.renderer.setStyle(this.el.nativeElement, 'user-select', '');
  }

  onMousedown(event: MouseEvent) {
    this.startX = event.pageX;
    this.startResizing();
  }

  private startResizing(): void {
    this.stopResizing();
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
  }

  private resize(event: MouseEvent): void {
    const target = this.resizeTarget();
    const movementX = event.pageX - this.startX;
    const newWidth = target.clientWidth + movementX;

    this.renderer.setStyle(target, 'min-width', `${newWidth}px`);
    this.renderer.setStyle(target, 'width', `${newWidth}px`);

    this.startX = event.pageX;
  }

  // A table column is as wide as its widest cell, so all cells of a column resize the same header cell.
  // Otherwise a cell widened in one row would keep the column from shrinking when dragged in another row.
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
