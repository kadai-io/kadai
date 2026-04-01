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

import { DragAndDropDirective } from './drag-and-drop.directive';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, expect, it, beforeEach, vi } from 'vitest';
import { By } from '@angular/platform-browser';

@Component({
  template: `<div kadaiDragAndDrop (onFileDropped)="onFilesDropped($event)"></div>`,
  imports: [DragAndDropDirective]
})
class TestHostComponent {
  droppedFiles: any = null;
  onFilesDropped(files: any) {
    this.droppedFiles = files;
  }
}

describe('DragAndDropDirective', () => {
  it('should create an instance', () => {
    const directive = new DragAndDropDirective();
    expect(directive).toBeTruthy();
  });

  describe('with TestBed', () => {
    let fixture: ComponentFixture<TestHostComponent>;
    let directiveEl: HTMLElement;
    let directive: DragAndDropDirective;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [TestHostComponent]
      }).compileComponents();

      fixture = TestBed.createComponent(TestHostComponent);
      fixture.detectChanges();

      const debugEl = fixture.debugElement.query(By.directive(DragAndDropDirective));
      directiveEl = debugEl.nativeElement;
      directive = debugEl.injector.get(DragAndDropDirective);
    });

    it('onDragOver should set fileOver to true and prevent/stop event', () => {
      const mockEvent = {
        preventDefault: vi.fn(),
        stopPropagation: vi.fn()
      };
      directive.onDragOver(mockEvent);
      expect(mockEvent.preventDefault).toHaveBeenCalled();
      expect(mockEvent.stopPropagation).toHaveBeenCalled();
      expect(directive.fileOver).toBe(true);
    });

    it('onDragLeave should set fileOver to false and prevent/stop event', () => {
      directive.fileOver = true;
      const mockEvent = {
        preventDefault: vi.fn(),
        stopPropagation: vi.fn()
      };
      directive.onDragLeave(mockEvent);
      expect(mockEvent.preventDefault).toHaveBeenCalled();
      expect(mockEvent.stopPropagation).toHaveBeenCalled();
      expect(directive.fileOver).toBe(false);
    });

    it('ondrop should emit files when files are dropped', () => {
      const mockFiles = { length: 1, 0: { name: 'test.json' } };
      const mockEvent = {
        preventDefault: vi.fn(),
        stopPropagation: vi.fn(),
        dataTransfer: { files: mockFiles }
      };
      const emitSpy = vi.spyOn(directive.onFileDropped, 'emit');
      directive.ondrop(mockEvent);
      expect(mockEvent.preventDefault).toHaveBeenCalled();
      expect(mockEvent.stopPropagation).toHaveBeenCalled();
      expect(directive.fileOver).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(mockFiles);
    });

    it('ondrop should not emit when no files are dropped', () => {
      const mockEvent = {
        preventDefault: vi.fn(),
        stopPropagation: vi.fn(),
        dataTransfer: { files: { length: 0 } }
      };
      const emitSpy = vi.spyOn(directive.onFileDropped, 'emit');
      directive.ondrop(mockEvent);
      expect(emitSpy).not.toHaveBeenCalled();
    });

    it('ondrop should propagate files to host component via emit', () => {
      const host = fixture.componentInstance;
      const mockFiles = { length: 2, 0: { name: 'a.json' }, 1: { name: 'b.json' } };
      const mockEvent = {
        preventDefault: vi.fn(),
        stopPropagation: vi.fn(),
        dataTransfer: { files: mockFiles }
      };
      directive.ondrop(mockEvent);
      fixture.detectChanges();
      expect(host.droppedFiles).toEqual(mockFiles);
    });
  });
});
