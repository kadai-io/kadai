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

export interface ClassificationQueryFilterParameter {
  name?: string[];
  'name-like'?: string[];
  key?: string[];
  category?: string[];
  domain?: string[];
  type?: string[];
  'custom-1-like'?: string[];
  'custom-2-like'?: string[];
  'custom-3-like'?: string[];
  'custom-4-like'?: string[];
  'custom-5-like'?: string[];
  'custom-6-like'?: string[];
  'custom-7-like'?: string[];
  'custom-8-like'?: string[];
}
