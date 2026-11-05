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

import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { describe, expect, it } from 'vitest';
import { SvgIconComponent } from './svg-icon.component';

@Component({
  imports: [SvgIconComponent],
  template: `<svg-icon class="a" src="icon.svg" [svgStyle]="{ 'width.px': width(), fill: 'red' }" />
    <svg-icon src="icon.svg" />`,
  styles: 'svg { display: block; }'
})
class HostComponent {
  width = signal(10);
}

describe('SvgIconComponent', () => {
  it('should inline the svg once per url and apply svgStyle', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const fixture = TestBed.createComponent(HostComponent);
    // whenStable() would wait for the pending request itself, so just run the first render.
    TestBed.tick();

    TestBed.inject(HttpTestingController).expectOne('icon.svg').flush('<svg viewBox="0 0 1 1"><path /></svg>');
    await fixture.whenStable();

    const [first, second] = fixture.nativeElement.querySelectorAll('svg-icon');
    const svg: SVGElement = first.querySelector('svg');
    expect(first.classList).toContain('a');
    expect(svg.style.width).toBe('10px');
    expect(svg.style.fill).toBe('red');
    expect(
      svg
        .querySelector('path')
        ?.getAttributeNames()
        .some((n) => n.startsWith('_ngcontent'))
    ).toBe(true);
    expect(second.querySelector('svg')).not.toBe(svg);

    fixture.componentInstance.width.set(20);
    await fixture.whenStable();
    expect(svg.style.width).toBe('20px');
  });
});
