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

import { ReportData } from '../models/report-data';

export const workbasketReportMock: ReportData = {
  meta: {
    name: 'WorkbasketPriorityReport',
    date: '2021-08-24T11:44:34.023901Z',
    header: ['<249', '250 - 500', '>501'],
    rowDesc: ['WORKBASKET'],
    sumRowDesc: 'Total'
  },
  rows: [
    {
      cells: [5, 0, 0],
      total: 5,
      depth: 0,
      desc: ['ADMIN'],
      display: true
    },
    {
      cells: [3, 5, 2],
      total: 10,
      depth: 0,
      desc: ['GPK_KSC'],
      display: true
    }
  ],
  sumRow: [
    {
      cells: [8, 5, 2],
      total: 15,
      depth: 0,
      desc: ['Total'],
      display: true
    }
  ]
};
