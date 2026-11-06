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
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  linkedSignal,
  OnInit,
  output,
  viewChild
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Page } from 'app/shared/models/page';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { Observable } from 'rxjs';
import { MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatAutocomplete, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MatOption } from '@angular/material/core';

@Component({
  selector: 'kadai-shared-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss'],
  imports: [MatPaginator, MatFormField, MatInput, FormsModule, MatAutocompleteTrigger, MatAutocomplete, MatOption]
})
export class PaginationComponent implements OnInit, AfterViewInit {
  page = input<Page>();
  type = input<String>();
  numberOfItems = input<number>();
  expanded = input(true);
  resetPaging = input<Observable<null>>();
  changePage = output<number>();

  paginator = viewChild<MatPaginator>(MatPaginator);

  private destroyRef = inject(DestroyRef);

  hasItems = computed(() => (this.numberOfItems() ?? 0) > 0);
  pageSelected = linkedSignal(() => this.page()?.number ?? 1);
  pageNumbers = computed(() => Array.from({ length: this.page()?.totalPages ?? 0 }, (_, i) => i + 1));
  filteredPages: string[] = [];

  ngOnInit() {
    const resetPaging = this.resetPaging();
    if (resetPaging) resetPaging.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.goToPage(1));
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
    this.pageSelected.set(1);
  }

  changeToPage(event: PageEvent) {
    let currentPageIndex = event.pageIndex;
    this.pageSelected.update((page) => (currentPageIndex > (event.previousPageIndex ?? 0) ? page + 1 : page - 1));
    this.changePage.emit(currentPageIndex + 1);
  }

  goToPage(page: number) {
    const paginator = this.paginator();
    if (paginator) paginator.pageIndex = page - 1;
    this.pageSelected.set(page);
    this.changePage.emit(page);
  }

  filter(filterValue: string | number) {
    const pageNumbers = this.pageNumbers().map(String);
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
}
