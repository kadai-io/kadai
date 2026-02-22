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

import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  effect,
  ElementRef,
  inject,
  input,
  OnDestroy,
  OnInit,
  output,
  untracked,
  viewChild
} from '@angular/core';
import { Page } from 'app/shared/models/page';
import { MatPaginator } from '@angular/material/paginator';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NgClass } from '@angular/common';
import { MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatAutocomplete, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MatOption } from '@angular/material/core';

@Component({
  selector: 'kadai-shared-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatPaginator,
    NgClass,
    MatFormField,
    MatInput,
    FormsModule,
    MatAutocompleteTrigger,
    MatAutocomplete,
    MatOption
  ]
})
export class PaginationComponent implements OnInit, AfterViewInit, OnDestroy {
  page = input<Page>();
  type = input<String>();
  numberOfItems = input<number>();
  expanded = input(true);
  resetPaging = input<Observable<null>>();
  changePage = output<number>();

  paginator = viewChild<MatPaginator>(MatPaginator);
  paginationWrapper = viewChild<ElementRef>('pagination');

  destroy$ = new Subject<void>();

  hasItems = true;
  pageSelected = 1;
  pageNumbers: number[];
  filteredPages: string[] = [];

  constructor() {
    effect(() => {
      const expanded = this.expanded();
      const paginationWrapper = this.paginationWrapper();
      const rangeLabel = paginationWrapper?.nativeElement?.querySelector('.mat-mdc-paginator-range-label');
      const container = paginationWrapper?.nativeElement?.querySelector('.mat-mdc-paginator-container');
      if (rangeLabel && container) {
        if (!expanded) {
          container.style.justifyContent = 'center';
          rangeLabel.style.display = 'none';
        } else {
          container.style.justifyContent = 'flex-end';
          rangeLabel.style.display = 'block';
        }
      }
    });

    effect(() => {
      const page = this.page();
      const numberOfItems = this.numberOfItems();
      untracked(() => {
        this.hasItems = (numberOfItems ?? 0) > 0;
        if (page) {
          this.pageSelected = page.number;
          this.updateGoto();
        }
      });
    });
  }

  ngOnInit() {
    const resetPaging = this.resetPaging();
    if (resetPaging) resetPaging.pipe(takeUntil(this.destroy$)).subscribe(() => this.goToPage(1));
  }

  ngAfterViewInit() {
    this.changeLabel();
  }

  changeLabel() {
    const paginator = this.paginator();
    if (!paginator) return;
    paginator._intl.itemsPerPageLabel = 'Per page';
    paginator._intl.getRangeLabel = (page: number, pageSize: number, length: number) => {
      page += 1;
      const start = pageSize * (page - 1) + 1;
      const end = pageSize * page < length ? pageSize * page : length;
      if (length === 0) {
        return 'loading...';
      } else {
        return `${start} - ${end} of ${length}`;
      }
    };
    this.pageSelected = 1;
  }

  changeToPage(event) {
    let currentPageIndex = event.pageIndex;
    if (currentPageIndex > event.previousPageIndex) {
      this.pageSelected += 1;
    } else {
      this.pageSelected -= 1;
    }
    this.changePage.emit(currentPageIndex + 1);
  }

  updateGoto() {
    this.pageNumbers = [];
    for (let i = 1; i <= this.page()?.totalPages; i++) {
      this.pageNumbers.push(i);
    }
  }

  goToPage(page: number) {
    const paginator = this.paginator();
    if (paginator) paginator.pageIndex = page - 1;
    this.pageSelected = page;
    this.changePage.emit(page);
  }

  filter(filterValue) {
    const pageNumbers = this.pageNumbers.map(String);
    this.filteredPages = pageNumbers.filter((value) => value.includes(filterValue.toString()));
    if (this.filteredPages.length === 0) {
      this.filteredPages = pageNumbers;
    }
  }

  onSelectText() {
    const input = document.getElementById('inputTypeAhead') as HTMLInputElement;
    input.focus();
    input.select();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
