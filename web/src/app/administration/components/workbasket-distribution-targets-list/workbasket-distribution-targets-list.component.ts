/*
 * Copyright [2025] [envite consulting GmbH]
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
  AfterContentChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  inject,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { isEqual } from 'lodash';
import { WorkbasketSummary } from 'app/shared/models/workbasket-summary';
import { expandDown } from 'app/shared/animations/expand.animation';
import { MatListOption, MatSelectionList } from '@angular/material/list';
import { CdkFixedSizeVirtualScroll, CdkVirtualForOf, CdkVirtualScrollViewport } from '@angular/cdk/scrolling';
import { Side } from '../../models/workbasket-distribution-enums';
import { Store } from '@ngxs/store';
import { WorkbasketSelectors } from '../../../shared/store/workbasket-store/workbasket.selectors';
import { filter, map, pairwise, take, takeUntil, throttleTime } from 'rxjs/operators';
import {
  FetchAvailableDistributionTargets,
  FetchWorkbasketDistributionTargets,
  TransferDistributionTargets
} from '../../../shared/store/workbasket-store/workbasket.actions';
import { Observable, Subject } from 'rxjs';
import { WorkbasketQueryFilterParameter } from '../../../shared/models/workbasket-query-filter-parameter';
import { FilterSelectors } from '../../../shared/store/filter-store/filter.selectors';
import { WorkbasketDistributionTarget } from '../../../shared/models/workbasket-distribution-target';
import { MatToolbar } from '@angular/material/toolbar';
import { MatTooltip } from '@angular/material/tooltip';
import { MatButton } from '@angular/material/button';

import { MatIcon } from '@angular/material/icon';
import { WorkbasketFilterComponent } from '../../../shared/components/workbasket-filter/workbasket-filter.component';
import { IconTypeComponent } from '../type-icon/icon-type.component';
import { MatDivider } from '@angular/material/divider';
import { OrderBy } from '../../../shared/pipes/order-by.pipe';

@Component({
  selector: 'kadai-administration-workbasket-distribution-targets-list',
  templateUrl: './workbasket-distribution-targets-list.component.html',
  styleUrls: ['./workbasket-distribution-targets-list.component.scss'],
  animations: [expandDown],
  imports: [
    MatToolbar,
    MatTooltip,
    MatButton,
    MatIcon,
    WorkbasketFilterComponent,
    MatSelectionList,
    CdkVirtualScrollViewport,
    CdkFixedSizeVirtualScroll,
    CdkVirtualForOf,
    MatListOption,
    IconTypeComponent,
    MatDivider,
    OrderBy
  ]
})
export class WorkbasketDistributionTargetsListComponent
  implements AfterContentChecked, OnChanges, OnInit, AfterViewInit
{
  @Input() side: Side;
  @Input() header: string;
  allSelected;
  @Input() component;
  @Input() transferDistributionTargetObservable: Observable<Side>;
  workbasketDistributionTargets$: Observable<WorkbasketSummary[]> = inject(Store).select(
    WorkbasketSelectors.workbasketDistributionTargets
  );
  availableDistributionTargets$: Observable<WorkbasketSummary[]> = inject(Store).select(
    WorkbasketSelectors.availableDistributionTargets
  );
  availableDistributionTargetsFilter$: Observable<WorkbasketQueryFilterParameter> = inject(Store).select(
    FilterSelectors.getAvailableDistributionTargetsFilter
  );
  selectedDistributionTargetsFilter$: Observable<WorkbasketQueryFilterParameter> = inject(Store).select(
    FilterSelectors.getSelectedDistributionTargetsFilter
  );
  toolbarState = false;
  distributionTargets: WorkbasketDistributionTarget[];
  distributionTargetsClone: WorkbasketDistributionTarget[];
  @ViewChild('workbasket') distributionTargetsList: MatSelectionList;
  @ViewChild('scroller') workbasketList: CdkVirtualScrollViewport;
  requestInProgress: number;
  private changeDetector = inject(ChangeDetectorRef);
  private store = inject(Store);
  private destroy$ = new Subject<void>();
  private filter: WorkbasketQueryFilterParameter;
  private allSelectedDiff = 0;

  ngOnInit(): void {
    this.requestInProgress = 2;
    if (this.side === Side.AVAILABLE) {
      this.availableDistributionTargets$.pipe(takeUntil(this.destroy$)).subscribe((wbs) => this.assignWbs(wbs));
      this.availableDistributionTargetsFilter$.pipe(takeUntil(this.destroy$)).subscribe((filter) => {
        if (typeof this.filter === 'undefined' || isEqual(this.filter, filter)) {
          this.filter = filter;
          return;
        }
        this.filter = filter;
        this.store.dispatch(new FetchAvailableDistributionTargets(true, this.filter));
        this.selectAll(false);
        this.requestInProgress--;
      });
    } else {
      this.workbasketDistributionTargets$.pipe().subscribe((wbs) => this.assignWbs(wbs));
      this.selectedDistributionTargetsFilter$.pipe(takeUntil(this.destroy$)).subscribe((filter) => {
        if (typeof this.filter === 'undefined' || isEqual(this.filter, filter)) {
          this.filter = filter;
          return;
        }
        this.filter = filter;
        this.applyFilter();
        this.selectAll(false);
        this.requestInProgress--;
      });
    }
    this.transferDistributionTargetObservable.subscribe((targetSide) => {
      if (targetSide !== this.side) this.transferDistributionTargets(targetSide);
    });
  }

  ngAfterContentChecked(): void {
    this.changeDetector.detectChanges();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (typeof changes.allSelected?.currentValue !== 'undefined') {
      this.selectAll(changes.allSelected.currentValue);
    }
  }

  ngAfterViewInit() {
    this.workbasketList
      .elementScrolled()
      .pipe(
        map(() => this.workbasketList.measureScrollOffset('bottom')),
        pairwise(),
        filter(([y1, y2]) => y2 < y1 && y2 < 270),
        throttleTime(200)
      )
      .subscribe(() => {
        if (this.side === Side.AVAILABLE) {
          this.store.dispatch(new FetchAvailableDistributionTargets(false, this.filter));
        } else {
          this.store.dispatch(new FetchWorkbasketDistributionTargets(false, this.filter));
        }
      });
  }

  selectAll(selected: boolean) {
    if (typeof this.distributionTargetsList !== 'undefined') {
      this.allSelected = selected;
      this.distributionTargets.map((wb) => (wb.selected = selected));
      if (selected) this.allSelectedDiff = this.distributionTargets.length;
      else this.allSelectedDiff = 0;
    }
  }

  transferDistributionTargets(targetSide: Side) {
    let selectedWBs = this.distributionTargets.filter((item: any) => item.selected === true);
    this.distributionTargets.forEach((wb) => (wb.selected = false));
    this.store
      .dispatch(new TransferDistributionTargets(targetSide, selectedWBs))
      .pipe(take(1))
      .subscribe(() => {
        if (this.distributionTargets.length === 0 && targetSide === Side.SELECTED) {
          this.store.dispatch(new FetchAvailableDistributionTargets(false, this.filter));
        }
      });
  }

  changeToolbarState(state: boolean) {
    this.toolbarState = state;
  }

  updateSelectAll(selected: boolean) {
    if (selected) this.allSelectedDiff++;
    else this.allSelectedDiff--;
    this.allSelected = this.allSelectedDiff === this.distributionTargets.length;
    return true;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private assignWbs(wbs: WorkbasketSummary[]) {
    this.distributionTargets = wbs.map((wb) => {
      return { ...wb, selected: this.allSelected };
    });
    this.distributionTargetsClone = this.distributionTargets;
    this.requestInProgress--;
  }

  private applyFilter() {
    function filterExact(target: WorkbasketDistributionTarget, filterStrings: string[], attribute: string) {
      if (!!filterStrings && filterStrings?.length !== 0) {
        return filterStrings.map((str) => str.toLowerCase()).includes(target[attribute].toLowerCase());
      }
      return true;
    }

    function filterLike(target: WorkbasketDistributionTarget, filterStrings: string[], attribute: string) {
      if (!!filterStrings && filterStrings?.length !== 0) {
        let ret = true;
        filterStrings.forEach((filterElement) => {
          ret = ret && target[attribute].toLowerCase().includes(filterElement.toLowerCase());
        });
        return ret;
      }
      return true;
    }

    this.distributionTargets = this.distributionTargetsClone?.filter((target) => {
      let matches = true;
      matches = matches && filterExact(target, this.filter.name, 'name');
      matches = matches && filterExact(target, this.filter.key, 'key');
      matches = matches && filterExact(target, this.filter.owner, 'owner');
      matches = matches && filterExact(target, this.filter.domain, 'domain');
      matches = matches && filterExact(target, this.filter.type, 'type');
      matches = matches && filterLike(target, this.filter['owner-like'], 'owner');
      matches = matches && filterLike(target, this.filter['name-like'], 'name');
      matches = matches && filterLike(target, this.filter['key-like'], 'key');
      matches = matches && filterLike(target, this.filter['description-like'], 'description');
      return matches;
    });
  }
}
