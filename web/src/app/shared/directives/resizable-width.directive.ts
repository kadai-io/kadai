/*
 * Copyright [2024] [envite consulting GmbH]
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

import { Directive, HostListener, Renderer2, ElementRef } from '@angular/core';

@Directive({
  selector: '[kadaiResizableWidth]'
})
export class ResizableWidthDirective {
  private startX: number;

  constructor(
    private renderer: Renderer2,
    private el: ElementRef
  ) {}

  @HostListener('mouseover', ['$event'])
  onMouseover() {
    this.renderer.setStyle(document.body, 'cursor', 'col-resize');
    this.renderer.setStyle(this.el.nativeElement, 'user-select', 'none');
  }

  @HostListener('mouseout', ['$event'])
  onMouseout() {
    this.renderer.setStyle(document.body, 'cursor', '');
    this.renderer.setStyle(this.el.nativeElement, 'user-select', '');
  }

  @HostListener('mousedown', ['$event'])
  onMousedown(event: MouseEvent) {
    this.startX = event.pageX;
  }

  @HostListener('document:mousemove', ['$event'])
  onMousemove(event: MouseEvent) {
    if (this.startX) {
      const movementX = event.pageX - this.startX;
      const newWidth = this.el.nativeElement.clientWidth + movementX;

      this.renderer.setStyle(this.el.nativeElement, 'min-width', `${newWidth}px`);
      this.renderer.setStyle(this.el.nativeElement, 'width', `${newWidth}px`);

      this.startX = event.pageX;
    }
  }

  @HostListener('document:mouseup')
  onMouseup() {
    this.startX = null;
  }
}
