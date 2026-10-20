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

import { Directive, ElementRef, inject, Renderer2 } from '@angular/core';

@Directive({
  selector: '[kadaiResizableWidth]',
  host: {
    '(mouseover)': 'onMouseover()',
    '(mouseout)': 'onMouseout()',
    '(mousedown)': 'onMousedown($event)',
    '(document:mousemove)': 'onMousemove($event)',
    '(document:mouseup)': 'onMouseup()'
  }
})
export class ResizableWidthDirective {
  private renderer = inject(Renderer2);
  private el = inject(ElementRef);

  private startX: number | null = null;

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
  }

  onMousemove(event: MouseEvent) {
    if (this.startX !== null) {
      const target = this.resizeTarget();
      const movementX = event.pageX - this.startX;
      const newWidth = target.clientWidth + movementX;

      this.renderer.setStyle(target, 'min-width', `${newWidth}px`);
      this.renderer.setStyle(target, 'width', `${newWidth}px`);

      this.startX = event.pageX;
    }
  }

  onMouseup() {
    this.startX = null;
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
