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

import { afterRenderEffect, Component, computed, ElementRef, inject, input, Renderer2 } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { SvgIconRegistryService } from './svg-icon-registry.service';

/** Inlines the SVG at `src` so it can be styled with CSS (replaces angular-svg-icon). */
@Component({
  selector: 'svg-icon',
  template: ''
})
export class SvgIconComponent {
  readonly src = input<string>();
  /** ngStyle-like map applied to the inner <svg>; keys may carry a unit suffix, e.g. 'width.px'. */
  readonly svgStyle = input<Record<string, string | number>>();

  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly renderer = inject(Renderer2);
  private readonly registry = inject(SvgIconRegistryService);

  // An undefined src keeps the resource idle, so nothing is fetched.
  private readonly svg = rxResource({
    params: () => this.src(),
    stream: ({ params }) => this.registry.loadSvg(params)
  });

  // Each component instance needs its own copy of the cached SVG element.
  private readonly icon = computed(() => {
    const svg = this.svg.hasValue() ? this.svg.value() : null;
    return svg ? (svg.cloneNode(true) as SVGElement) : undefined;
  });

  constructor() {
    afterRenderEffect({
      write: () => {
        const icon = this.icon();
        if (!icon) return;
        const host = this.element.nativeElement;
        if (icon.parentNode !== host) this.insert(host, icon);
        // shortcut: styles removed from svgStyle are not cleared; all current callers use fixed keys
        for (const [key, value] of Object.entries(this.svgStyle() ?? {})) {
          const [name, unit] = key.split('.');
          this.renderer.setStyle(icon, name, unit ? `${value}${unit}` : value);
        }
      }
    });
  }

  private insert(host: HTMLElement, icon: SVGElement) {
    host.replaceChildren();
    this.renderer.appendChild(host, icon);
    // Let the host component's encapsulated styles reach the inlined SVG.
    const ngContent = Array.from(host.attributes).find((a) => a.name.startsWith('_ngcontent'));
    if (ngContent) {
      [icon, ...Array.from(icon.querySelectorAll('*'))].forEach((el) =>
        this.renderer.setAttribute(el, ngContent.name, '')
      );
    }
  }
}
