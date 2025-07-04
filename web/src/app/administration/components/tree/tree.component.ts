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
  AfterViewChecked,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { TreeNodeModel } from 'app/administration/models/tree-node';

import { ITreeOptions, KEYS, TREE_ACTIONS, TreeComponent, TreeModule } from '@ali-hm/angular-tree-component';
import { combineLatest, Observable, Subject } from 'rxjs';
import { filter, map, takeUntil } from 'rxjs/operators';
import { Store } from '@ngxs/store';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';

import { Location } from '@angular/common';
import { NotificationService } from 'app/shared/services/notifications/notification.service';
import { Classification } from '../../../shared/models/classification';
import { ClassificationsService } from '../../../shared/services/classifications/classifications.service';
import { ClassificationCategoryImages } from '../../../shared/models/customisation';
import { ClassificationSelectors } from '../../../shared/store/classification-store/classification.selectors';
import {
  DeselectClassification,
  SelectClassification,
  UpdateClassification
} from '../../../shared/store/classification-store/classification.actions';
import { ClassificationTreeService } from '../../services/classification-tree.service';
import { Pair } from '../../../shared/models/pair';
import { RequestInProgressService } from '../../../shared/services/request-in-progress/request-in-progress.service';
import { SvgIconComponent } from 'angular-svg-icon';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'kadai-administration-tree',
  templateUrl: './tree.component.html',
  styleUrls: ['./tree.component.scss'],
  imports: [TreeModule, SvgIconComponent, MatTooltip]
})
export class KadaiTreeComponent implements OnInit, AfterViewChecked, OnDestroy {
  treeNodes: TreeNodeModel[];
  categoryIcons: ClassificationCategoryImages;
  emptyTreeNodes = false;
  filter: string;
  category: string;
  @Input() selectNodeId: string;
  @Input() filterText: string;
  @Input() filterIcon = '';
  @Output() switchKadaiSpinnerEmit = new EventEmitter<boolean>();
  categoryIcons$: Observable<ClassificationCategoryImages> = inject(Store).select(
    EngineConfigurationSelectors.selectCategoryIcons
  );
  selectedClassificationId$: Observable<string> = inject(Store).select(
    ClassificationSelectors.selectedClassificationId
  );
  classifications$: Observable<Classification[]> = inject(Store).select(ClassificationSelectors.classifications);
  classificationTypeSelected$: Observable<string> = inject(Store).select(
    ClassificationSelectors.selectedClassificationType
  );
  options: ITreeOptions = {
    displayField: 'name',
    idField: 'classificationId',
    actionMapping: {
      keys: {
        [KEYS.ENTER]: TREE_ACTIONS.TOGGLE_ACTIVE,
        [KEYS.SPACE]: TREE_ACTIONS.TOGGLE_EXPANDED
      }
    },
    useVirtualScroll: true,
    scrollOnActivate: false,
    levelPadding: 20,
    allowDrag: true,
    allowDrop: true
  };
  private elementRef = inject(ElementRef);
  private classificationsService = inject(ClassificationsService);
  private location = inject(Location);
  private store = inject(Store);
  private notificationsService = inject(NotificationService);
  private classificationTreeService = inject(ClassificationTreeService);
  private requestInProgressService = inject(RequestInProgressService);
  @ViewChild('tree', { static: true })
  private tree: TreeComponent;

  private filterTextOld: string;
  private filterIconOld = '';
  private destroy$ = new Subject<void>();

  @HostListener('document:click', ['$event'])
  onDocumentClick(event) {
    if (this.checkValidElements(event) && this.tree.treeModel.getActiveNode()) {
      this.deselectActiveNode();
    }
  }

  ngOnInit() {
    const computedTreeNodes$: Observable<TreeNodeModel[]> = this.classifications$.pipe(
      filter((classifications) => typeof classifications !== 'undefined'),
      map((classifications) => this.classificationTreeService.transformToTreeNode(classifications))
    );

    combineLatest([this.selectedClassificationId$, computedTreeNodes$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([selectedClassificationId, treeNodes]) => {
        this.treeNodes = treeNodes;
        this.selectNodeId = typeof selectedClassificationId !== 'undefined' ? selectedClassificationId : undefined;
        this.requestInProgressService.setRequestInProgress(false);
        if (typeof this.tree.treeModel.getActiveNode() !== 'undefined') {
          if (this.tree.treeModel.getActiveNode().data.classificationId !== this.selectNodeId) {
            // wait for angular's two-way binding to convert the treeNodes to the internal tree structure.
            // after that conversion the new treeNodes are available
            setTimeout(() => this.selectNode(this.selectNodeId), 0);
          }
        }
      });

    this.classificationTypeSelected$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      if (this.tree.treeModel.getActiveNode()) {
        this.deselectActiveNode();
      }
    });

    this.categoryIcons$.pipe(takeUntil(this.destroy$)).subscribe((categoryIcons) => {
      this.categoryIcons = categoryIcons;
    });
  }

  ngAfterViewChecked(): void {
    if (this.selectNodeId && !this.tree.treeModel.getActiveNode()) {
      this.selectNode(this.selectNodeId);
    }

    if (typeof this.selectNodeId !== 'undefined') {
      if (typeof this.getNode(this.selectNodeId) !== 'undefined') {
        this.getNode(this.selectNodeId).ensureVisible();
      }
    }

    if (this.filterTextOld !== this.filterText || this.filterIconOld !== this.filterIcon) {
      this.filterIconOld = this.filterIcon;
      this.filterTextOld = this.filterText;
      this.filterNodes(this.filterText ? this.filterText : '', this.filterIcon);
      this.manageTreeState();
    }
  }

  onActivate(treeNode: any) {
    const id = treeNode.node.data.classificationId;
    this.selectNodeId = id;
    this.requestInProgressService.setRequestInProgress(true);
    this.store.dispatch(new SelectClassification(id));
    this.location.go(this.location.path().replace(/(classifications).*/g, `classifications/(detail:${id})`));
  }

  onDeactivate(event: any) {
    if (!event.treeModel.activeNodes.length) {
      this.store.dispatch(new DeselectClassification());
      this.location.go(this.location.path().replace(/(classifications).*/g, 'classifications'));
    }
  }

  async onMoveNode($event) {
    this.switchKadaiSpinner(true);
    const classification = await this.getClassification($event.node.classificationId);
    classification.parentId = $event.to.parent.classificationId;
    classification.parentKey = $event.to.parent.key;
    this.collapseParentNodeIfItIsTheLastChild($event.node);
    this.updateClassification(classification);
  }

  async onDrop($event) {
    if ($event.event.target.tagName === 'TREE-VIEWPORT') {
      this.switchKadaiSpinner(true);
      const classification = await this.getClassification($event.element.data.classificationId);
      this.collapseParentNodeIfItIsTheLastChild($event.element.data);
      classification.parentId = '';
      classification.parentKey = '';
      this.updateClassification(classification);
    }
  }

  getCategoryIcon(category: string): Pair<string, string> {
    return this.categoryIcons[category]
      ? { left: this.categoryIcons[category], right: category }
      : { left: this.categoryIcons.missing, right: 'Category does not match with the configuration' };
  }

  switchKadaiSpinner(active: boolean) {
    this.switchKadaiSpinnerEmit.emit(active);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private checkNameAndKey(node: any, text: string): boolean {
    return (
      node.data.name.toUpperCase().includes(text.toUpperCase()) ||
      node.data.key.toUpperCase().includes(text.toUpperCase())
    );
  }

  private checkIcon(node: any, iconText: string): boolean {
    return node.data.category.toUpperCase() === iconText.toUpperCase() || iconText === '';
  }

  private selectNode(nodeId: string) {
    if (nodeId) {
      const selectedNode = this.getNode(nodeId);
      if (selectedNode) {
        selectedNode.setIsActive(true);
      }
    }
  }

  private deselectActiveNode() {
    const activeNode = this.tree.treeModel.getActiveNode();
    delete this.selectNodeId;
    activeNode.setIsActive(false);
    activeNode.blur();
  }

  private getNode(nodeId: string) {
    return this.tree.treeModel.getNodeById(nodeId);
  }

  private filterNodes(filterText, category) {
    this.tree.treeModel.filterNodes((node) => this.checkNameAndKey(node, filterText) && this.checkIcon(node, category));
    this.filter = filterText;
    this.category = category || 'ALL';
    this.emptyTreeNodes = !this.tree.treeModel.getVisibleRoots().length;
  }

  private manageTreeState() {
    this.tree.treeModel.collapseAll();
    if (this.filterText === '') {
      this.tree.treeModel.collapseAll();
    }
  }

  private checkValidElements(event): boolean {
    return (
      (this.elementRef.nativeElement.contains(event.target) || this.elementRef.nativeElement === event.target) &&
      (event.target.localName === 'tree-viewport' || event.target.localName === 'kadai-tree')
    );
  }

  private getClassification(classificationId: string): Promise<Classification> {
    return this.classificationsService.getClassification(classificationId).toPromise();
  }

  private updateClassification(classification: Classification) {
    this.store.dispatch(new UpdateClassification(classification)).subscribe(() => {
      this.notificationsService.showSuccess('CLASSIFICATION_MOVE', { classificationKey: classification.key });
      this.switchKadaiSpinner(false);
    });
  }

  private collapseParentNodeIfItIsTheLastChild(node: any) {
    if (node.parentId.length > 0 && this.getNode(node.parentId) && this.getNode(node.parentId).children.length < 2) {
      this.tree.treeModel.update();
      this.getNode(node.parentId).collapse();
    }
  }
}
