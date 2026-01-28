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

import { Workbasket } from 'app/shared/models/workbasket';
import { ObjectReference } from './object-reference';
import { ClassificationSummary } from '../../shared/models/classification-summary';

export class Task {
  constructor(
    public taskId: string,
    public primaryObjRef?: ObjectReference,
    public workbasketSummary?: Workbasket,
    public classificationSummary?: ClassificationSummary,
    public businessProcessId?: string,
    public parentBusinessProcessId?: string,
    public owner?: string,
    public created?: string, // ISO-8601
    public claimed?: string, // ISO-8601
    public completed?: string, // ISO-8601
    public modified?: string, // ISO-8601
    public planned?: string, // ISO-8601
    public received?: string, // ISO-8601
    public due?: string, // ISO-8601
    public name?: string,
    public creator?: string,
    public description?: string,
    public note?: string,
    public state?: any,
    public read?: boolean,
    public transferred?: boolean,
    public priority?: number,
    public customAttributes: CustomAttribute[] = [],
    public callbackInfo: CustomAttribute[] = [],
    public custom1?: string,
    public custom2?: string,
    public custom3?: string,
    public custom4?: string,
    public custom5?: string,
    public custom6?: string,
    public custom7?: string,
    public custom8?: string,
    public custom9?: string,
    public custom10?: string,
    public custom11?: string,
    public custom12?: string,
    public custom13?: string,
    public custom14?: string,
    public custom15?: string,
    public custom16?: string
  ) {}
}

export class CustomAttribute {
  key: string;
  value: string;
}
