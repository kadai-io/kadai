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

export function asUrlQueryString(params: Object): string {
  let query = '';

  for (const [key, value] of Object.entries(params)) {
    if (value) {
      let values: any[] = value instanceof Array ? value : [value];
      values
        .filter((v) => v !== undefined)
        .forEach((v) => (query += (query ? '&' : '?') + `${key}=${convertValue(v)}`));
    }
  }
  return query;
}

function convertValue(value: any) {
  if (value instanceof Object) {
    return encodeURIComponent(JSON.stringify(value));
  }
  return value;
}
