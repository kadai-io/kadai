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
  AfterViewChecked,
  AfterViewInit,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  input,
  linkedSignal,
  output,
  signal,
  viewChild
} from '@angular/core';
import { TreeNodeModel } from 'app/administration/models/tree-node';

import {
  ITreeOptions,
  KEYS,
  TREE_ACTIONS,
  TreeComponent,
  TreeModel,
  TreeModule,
  TreeNode
} from '@ali-hm/angular-tree-component';
import { combineLatest, firstValueFrom, Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Store } from '@ngxs/store';
import { EngineConfigurationSelectors } from 'app/shared/store/engine-configuration-store/engine-configuration.selectors';

import { Location } from '@angular/common';
import { NotificationService } from 'app/shared/services/notifications/notification.service';
import { Classification } from 'app/shared/models/classification';
import { ClassificationsService } from 'app/shared/services/classifications/classifications.service';
import { ClassificationSelectors } from 'app/shared/store/classification-store/classification.selectors';
import {
  DeselectClassification,
  SelectClassification,
  UpdateClassification
} from 'app/shared/store/classification-store/classification.actions';
import { ClassificationTreeService } from 'app/administration/services/classification-tree.service';
import { Pair } from 'app/shared/models/pair';
import { RequestInProgressService } from 'app/shared/services/request-in-progress/request-in-progress.service';
import { SvgIconComponent } from 'app/shared/components/svg-icon/svg-icon.component';
import { MatTooltip } from '@angular/material/tooltip';
import { toSignal, takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'kadai-administration-tree',
  templateUrl: './tree.component.html',
  styleUrls: ['./tree.component.scss'],
  imports: [TreeModule, SvgIconComponent, MatTooltip],
  host: { '(document:click)': 'onDocumentClick($event)' }
})
export class KadaiTreeComponent implements AfterViewInit, AfterViewChecked {
  treeNodes = signal<TreeNodeModel[] | undefined>(undefined);
  categoryIcons = toSignal(inject(Store).select(EngineConfigurationSelectors.selectCategoryIcons), {
    requireSync: true
  });
  emptyTreeNodes = signal(false);
  filter = signal('');
  category = signal('');
  filterText = input<string>();
  filterIcon = input('');
  switchKadaiSpinnerEmit = output<boolean>();
  selectedClassificationId$: Observable<string | undefined> = inject(Store).select(
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
  private tree = viewChild.required<TreeComponent>('tree');
  _selectNodeIdInput = input<string>(undefined, { alias: 'selectNodeId' });
  selectNodeId = linkedSignal<string | undefined>(() => this._selectNodeIdInput());

  private filterTextOld?: string;
  private filterIconOld = '';
  private treeModelNodesOld?: TreeNodeModel[];
  private destroyRef = inject(DestroyRef);

  onDocumentClick(event: MouseEvent) {
    if (this.checkValidElements(event) && this.tree().treeModel.getActiveNode()) {
      this.deselectActiveNode();
    }
  }

  ngAfterViewInit() {
    const computedTreeNodes$: Observable<TreeNodeModel[]> = this.classifications$.pipe(
      filter((classifications) => typeof classifications !== 'undefined'),
      map((classifications) => this.classificationTreeService.transformToTreeNode(classifications))
    );

    combineLatest([this.selectedClassificationId$, computedTreeNodes$])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(([selectedClassificationId, treeNodes]) => {
        this.treeNodes.set(treeNodes);
        this.selectNodeId.set(typeof selectedClassificationId !== 'undefined' ? selectedClassificationId : undefined);
        this.requestInProgressService.setRequestInProgress(false);
        if (typeof this.tree().treeModel.getActiveNode() !== 'undefined') {
          if (this.tree().treeModel.getActiveNode().data.classificationId !== this.selectNodeId()) {
            // wait for angular's two-way binding to convert the treeNodes to the internal tree structure.
            // after that conversion the new treeNodes are available
            setTimeout(() => this.selectNode(this.selectNodeId()), 0);
          }
        }
      });

    this.classificationTypeSelected$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.tree().treeModel.getActiveNode()) {
        this.deselectActiveNode();
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.selectNodeId() && !this.tree().treeModel.getActiveNode()) {
      this.selectNode(this.selectNodeId());
    }

    const selectedNodeId = this.selectNodeId();
    if (selectedNodeId != null) {
      const node = this.getNode(selectedNodeId);
      if (node != null) {
        node.ensureVisible();
      }
    }

    // The tree model receives the nodes via the [nodes] binding, so by the time this hook
    // runs it reflects what the tree actually rendered. Re-evaluate the filter whenever the
    // filter inputs or the rendered nodes change; evaluating against the signal instead
    // would run one change detection pass too early.
    const treeModelNodes = this.tree().treeModel.nodes as TreeNodeModel[] | undefined;
    const filterChanged = this.filterTextOld !== this.filterText() || this.filterIconOld !== this.filterIcon();
    const treeModelNodesChanged = treeModelNodes !== this.treeModelNodesOld;
    if (filterChanged || treeModelNodesChanged) {
      this.filterIconOld = this.filterIcon();
      this.filterTextOld = this.filterText();
      this.treeModelNodesOld = treeModelNodes;
      if (treeModelNodes && treeModelNodes.length > 0) {
        this.filterNodes(this.filterText() ?? '', this.filterIcon());
        if (filterChanged) {
          this.manageTreeState();
        }
      }
    }
  }

  onActivate(treeNode: { node: TreeNode }) {
    const id = treeNode.node.data.classificationId;
    this.selectNodeId.set(id);
    this.requestInProgressService.setRequestInProgress(true);
    this.store.dispatch(new SelectClassification(id));
    this.location.go(this.location.path().replace(/(classifications).*/g, `classifications/(detail:${id})`));
  }

  onDeactivate(event: { treeModel: TreeModel }) {
    if (!event.treeModel.activeNodes.length) {
      this.store.dispatch(new DeselectClassification());
      this.location.go(this.location.path().replace(/(classifications).*/g, 'classifications'));
    }
  }

  async onMoveNode($event: { node: TreeNodeModel; to: { parent: TreeNodeModel } }) {
    this.switchKadaiSpinner(true);
    const classification = await this.getClassification($event.node.classificationId!);
    classification.parentId = $event.to.parent.classificationId;
    classification.parentKey = $event.to.parent.key;
    this.collapseParentNodeIfItIsTheLastChild($event.node);
    this.updateClassification(classification);
  }

  async onDrop($event: { event: DragEvent; element: { data: TreeNodeModel } }) {
    if (($event.event.target as HTMLElement | null)?.tagName === 'TREE-VIEWPORT') {
      this.switchKadaiSpinner(true);
      const classification = await this.getClassification($event.element.data.classificationId!);
      this.collapseParentNodeIfItIsTheLastChild($event.element.data);
      classification.parentId = '';
      classification.parentKey = '';
      this.updateClassification(classification);
    }
  }

  getCategoryIcon(category: string): Pair<string, string> {
    return this.categoryIcons()[category]
      ? { left: this.categoryIcons()[category], right: category }
      : { left: this.categoryIcons().missing, right: 'Category does not match with the configuration' };
  }

  switchKadaiSpinner(active: boolean) {
    this.switchKadaiSpinnerEmit.emit(active);
  }

  private checkNameAndKey(node: TreeNode, text: string): boolean {
    return (
      node.data.name.toUpperCase().includes(text.toUpperCase()) ||
      node.data.key.toUpperCase().includes(text.toUpperCase())
    );
  }

  private checkIcon(node: TreeNode, iconText: string): boolean {
    return node.data.category.toUpperCase() === iconText.toUpperCase() || iconText === '';
  }

  private selectNode(nodeId: string | undefined) {
    if (nodeId) {
      const selectedNode = this.getNode(nodeId);
      if (selectedNode) {
        selectedNode.setIsActive(true);
      }
    }
  }

  private deselectActiveNode() {
    const activeNode = this.tree().treeModel.getActiveNode();
    this.selectNodeId.set(undefined);
    activeNode.setIsActive(false);
    activeNode.blur();
  }

  private getNode(nodeId: string) {
    return this.tree().treeModel.getNodeById(nodeId);
  }

  private filterNodes(filterText: string, category: string) {
    const isFiltering = !!filterText || !!category;
    this.tree().treeModel.filterNodes(
      (node: TreeNode) => this.checkNameAndKey(node, filterText) && this.checkIcon(node, category),
      isFiltering
    );
    this.filter.set(filterText);
    this.category.set(category || 'ALL');
    this.emptyTreeNodes.set(!this.tree().treeModel.getVisibleRoots()?.length);
  }

  private manageTreeState() {
    this.tree().treeModel.collapseAll();
    if (this.filterText() === '') {
      this.tree().treeModel.collapseAll();
    }
  }

  private checkValidElements(event: MouseEvent): boolean {
    const target = event.target as HTMLElement;
    return (
      (this.elementRef.nativeElement.contains(target) || this.elementRef.nativeElement === target) &&
      (target.localName === 'tree-viewport' || target.localName === 'kadai-tree')
    );
  }

  private getClassification(classificationId: string): Promise<Classification> {
    return firstValueFrom(this.classificationsService.getClassification(classificationId));
  }

  private updateClassification(classification: Classification) {
    this.store.dispatch(new UpdateClassification(classification)).subscribe(() => {
      this.notificationsService.showSuccess('CLASSIFICATION_MOVE', { classificationKey: classification.key });
      this.switchKadaiSpinner(false);
    });
  }

  private collapseParentNodeIfItIsTheLastChild(node: TreeNodeModel) {
    const parent = node.parentId ? this.getNode(node.parentId) : undefined;
    if (parent && parent.children.length < 2) {
      this.tree().treeModel.update();
      parent.collapse();
    }
  }
}
