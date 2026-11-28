/*
Sustained variant of the Blue Angel standard usage scenario (../blue_angel/3_standard_usage_scenario.js),
used by TaskUpdatePriorityJobTrafficBenchmarkRunner to measure the API while the TaskUpdatePriorityJob
runs. Same steps, users and 4 s pauses as the original, but:
- every user repeats the steps for DURATION instead of running them once,
- the users start evenly spread over one cycle, so the load is steady instead of synchronized bursts,
- the single monitor user retrieves the task status report once per cycle,
- workbaskets, classifications and open tasks come from the DATA file the runner writes, as discovering
  them through the API would scan the whole task table at production size.

The response times of each step go to the RESULTS file as tab-separated values.

Steps per cycle of 56 s (13 steps by every user, the report by the monitor user):
1. Create a new task
2. Search for workbaskets with a specific permission (OPEN)
3. Open a workbasket
4. Retrieve the first 50 tasks from a workbasket
5. Read a single task
6. Edit a task
7. Transfer a task to another workbasket
8. Assign task (Claim)
9. Retrieve comments for task
10. Add new comment to task
11. Delete comment
12. Complete task
13. Search for tasks by POR
14. Retrieve the task status report (Monitoring, single user)
*/

import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Counter, Trend } from 'k6/metrics';
import { authHeaders, baseUrl, randomItem } from '../discovery.js';

const BASE_URL = baseUrl();
const USERS = Number(__ENV.USERS || '100');
const DURATION = __ENV.DURATION || '10m';
const PAUSE_MS = 4000;
const CYCLE_MS = 14 * PAUSE_MS;
const MAX_LOGGED_FAILURES = 5;

const STEPS = [
  ['create_task', 'Create task'],
  ['search_workbaskets', 'Search workbaskets (OPEN)'],
  ['open_workbasket', 'Open workbasket'],
  ['workbasket_tasks', 'First 50 tasks of workbasket'],
  ['read_task', 'Read task'],
  ['edit_task', 'Edit task'],
  ['transfer_task', 'Transfer task'],
  ['claim_task', 'Claim task'],
  ['read_comments', 'Read comments'],
  ['add_comment', 'Add comment'],
  ['delete_comment', 'Delete comment'],
  ['complete_task', 'Complete task'],
  ['search_by_por', 'Search tasks by POR'],
  ['task_status_report', 'Task status report'],
];

const durations = {};
const failures = {};
for (const [step] of STEPS) {
  durations[step] = new Trend(`step_${step}`, true);
  failures[step] = new Counter(`failed_${step}`);
}

const data = new SharedArray('data', () => [JSON.parse(open(__ENV.DATA))])[0];
const workbasketsByDomain = groupByDomain(data.workbaskets);
const classificationsByDomain = groupByDomain(data.classifications);

export const options = {
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'max', 'count'],
  scenarios: {
    users: {
      executor: 'constant-vus',
      exec: 'user',
      vus: USERS,
      duration: DURATION,
      gracefulStop: '70s',
    },
    monitor: {
      executor: 'constant-vus',
      exec: 'monitor',
      vus: 1,
      duration: DURATION,
      gracefulStop: '70s',
    },
  },
};

let loggedFailures = 0;

function groupByDomain(items) {
  const byDomain = {};
  for (const item of items) {
    (byDomain[item.domain] = byDomain[item.domain] || []).push(item);
  }
  return byDomain;
}

function waitUntil(time) {
  const waitMs = time - Date.now();
  if (waitMs > 0) {
    sleep(waitMs / 1000);
  }
}

function measure(step, response, expectedStatus) {
  const ok = response.status === expectedStatus;
  durations[step].add(response.timings.duration);
  if (!ok) {
    failures[step].add(1);
    if (loggedFailures++ < MAX_LOGGED_FAILURES) {
      console.warn(`${step} failed: HTTP ${response.status} ${String(response.body).substring(0, 300)}`);
    }
  }
  check(response, { [step]: () => ok });
  return ok;
}

function request(method, path, body) {
  return http.request(method, `${BASE_URL}${path}`, body, { headers: authHeaders() });
}

export function user() {
  if (__ITER === 0) {
    sleep((((__VU - 1) % USERS) * CYCLE_MS) / USERS / 1000);
  }
  const start = Date.now();
  const at = (step) => waitUntil(start + step * PAUSE_MS);

  // 1. Create a new task
  const workbasket = randomItem(data.workbaskets);
  const classification = randomItem(classificationsByDomain[workbasket.domain]);
  const createRes = request('POST', '/tasks', JSON.stringify({
    name: `TestTask_${__VU}`,
    primaryObjRef: {
      company: 'test', system: 'test', systemInstance: 'test', type: 'test', value: `value_${__VU}`,
    },
    workbasketSummary: workbasket,
    classificationSummary: classification,
  }));
  if (!measure('create_task', createRes, 201)) {
    waitUntil(start + CYCLE_MS);
    return;
  }
  const task = createRes.json();
  const taskId = encodeURIComponent(task.taskId);

  // 2. Search for workbaskets with a specific permission (OPEN)
  at(1);
  measure('search_workbaskets', request('GET', '/workbaskets?required-permission=OPEN'), 200);

  // 3. Open a workbasket
  at(2);
  const workbasketToRead = encodeURIComponent(randomItem(data.browsedWorkbasketIds));
  measure('open_workbasket', request('GET', `/workbaskets/${workbasketToRead}`), 200);

  // 4. Retrieve the first 50 tasks from a workbasket
  at(3);
  measure(
    'workbasket_tasks',
    request('GET', `/tasks?page=1&page-size=50&workbasket-id=${workbasketToRead}`),
    200);

  // 5. Read a single task
  at(4);
  measure('read_task', request('GET', `/tasks/${encodeURIComponent(randomItem(data.taskIds))}`), 200);

  // 6. Edit a task
  at(5);
  task.note = 'Updated by k6';
  measure('edit_task', request('PUT', `/tasks/${taskId}`, JSON.stringify(task)), 200);

  // 7. Transfer a task to another workbasket of its domain
  at(6);
  const target = encodeURIComponent(randomItem(workbasketsByDomain[workbasket.domain]).workbasketId);
  measure('transfer_task', request('POST', `/tasks/${taskId}/transfer/${target}`, '{}'), 200);

  // 8. Assign task (Claim)
  at(7);
  measure('claim_task', request('POST', `/tasks/${taskId}/claim`, null), 200);

  // 9. Retrieve comments for task
  at(8);
  measure('read_comments', request('GET', `/tasks/${taskId}/comments`), 200);

  // 10. Add new comment to task
  at(9);
  const commentRes = request('POST', `/tasks/${taskId}/comments`, JSON.stringify({
    taskId: task.taskId,
    textField: `Kommentar von VU ${__VU}`,
    creator: 'k6-script',
    creatorFullName: 'K6 Load Test',
  }));

  // 11. Delete comment
  at(10);
  if (measure('add_comment', commentRes, 201)) {
    const commentId = encodeURIComponent(commentRes.json().taskCommentId);
    measure('delete_comment', request('DELETE', `/tasks/comments/${commentId}`, null), 204);
  }

  // 12. Complete task
  at(11);
  measure('complete_task', request('POST', `/tasks/${taskId}/complete`, null), 200);

  // 13. Search for tasks by POR
  at(12);
  const porValue = encodeURIComponent(task.primaryObjRef?.value ?? '');
  measure(
    'search_by_por',
    request('GET', `/tasks?por-type=${encodeURIComponent('Object Type')}&por-value=${porValue}`),
    200);

  waitUntil(start + CYCLE_MS);
}

// 14. Retrieve the task status report (by a single user)
export function monitor() {
  const start = Date.now();
  measure('task_status_report', request('GET', '/monitor/task-status-report'), 200);
  waitUntil(start + CYCLE_MS);
}

function value(metric, stat) {
  return metric?.values?.[stat] ?? 0;
}

export function handleSummary(summary) {
  const columns = ['step', 'label', 'requests', 'failed', 'avg', 'med', 'p95', 'p99', 'max'];
  const rows = STEPS.map(([step, label]) => {
    const trend = summary.metrics[`step_${step}`];
    return [
      step,
      label,
      value(trend, 'count'),
      value(summary.metrics[`failed_${step}`], 'count'),
      value(trend, 'avg'),
      value(trend, 'med'),
      value(trend, 'p(95)'),
      value(trend, 'p(99)'),
      value(trend, 'max'),
    ];
  });
  const all = summary.metrics.http_req_duration;
  rows.push([
    'all',
    'All requests',
    value(summary.metrics.http_reqs, 'count'),
    rows.reduce((sum, row) => sum + row[3], 0),
    value(all, 'avg'),
    value(all, 'med'),
    value(all, 'p(95)'),
    value(all, 'p(99)'),
    value(all, 'max'),
  ]);
  const tsv = [columns, ...rows].map((row) => row.join('\t')).join('\n') + '\n';
  return { [__ENV.RESULTS]: tsv };
}
