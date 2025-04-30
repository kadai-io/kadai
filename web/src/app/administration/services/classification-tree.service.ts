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

import { Injectable } from '@angular/core';
import { TreeNodeModel } from '../models/tree-node';
import { Classification } from '../../shared/models/classification';

@Injectable({
  providedIn: 'root'
})
export class ClassificationTreeService {
  transformToTreeNode(classifications: Classification[]): TreeNodeModel[] {
    const classificationsAsTree: TreeNodeModel[] = classifications
      .map((c) => ({
        ...c,
        children: []
      }))
      .sort((a: TreeNodeModel, b: TreeNodeModel) => a.key.localeCompare(b.key));
    const roots: TreeNodeModel[] = [];
    const children: TreeNodeModel[] = [];
    classificationsAsTree.forEach((item) => {
      const parent = item.parentId;
      const target = !parent ? roots : children[parent] || (children[parent] = []);
      target.push(item);
    });
    roots.forEach((parent) => this.findChildren(parent, children));
    return roots;
  }

  private findChildren(parent: TreeNodeModel, children: TreeNodeModel[]) {
    if (children[parent.classificationId]) {
      parent.children = children[parent.classificationId];
      parent.children.forEach((child) => this.findChildren(child, children));
    }
  }
}
