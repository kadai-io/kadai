/* 
The scenario developed as part of the Blue Angel certification process reflects a realistic usage pattern of KADAI.
Multiple users perform the individual actions in parallel:
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

A single user performs the following action:
1. Retrieve the task status report (Monitoring)
*/

import http from 'k6/http';
import { check, sleep } from 'k6';
import { authHeaders, baseUrl, discover, randomItem } from '../discovery.js';

// Use custom summary
export { handleSummary } from "../summary.js";

const BASE_URL = baseUrl();
const VU_COUNT = 100;
const PAUSE_MS = 4000;

export const options = {
  scenarios: {
    default: {
      executor: 'per-vu-iterations',
      vus: VU_COUNT,
      iterations: 1, // each VU runs once
    },
  },
};

export function setup() {
  return discover();
}

function workbasketSummary(workbasket) {
  return {
    workbasketId: workbasket.workbasketId,
    key: workbasket.key,
    name: workbasket.name,
    domain: workbasket.domain,
    type: workbasket.type,
    description: workbasket.description,
    owner: workbasket.owner,
    markedForDeletion: workbasket.markedForDeletion,
  };
}

function classificationSummary(classification) {
  return {
    classificationId: classification.classificationId,
    key: classification.key,
    category: classification.category,
    domain: classification.domain,
    name: classification.name,
    priority: classification.priority,
    serviceLevel: classification.serviceLevel,
    type: classification.type,
    custom1: classification.custom1,
  };
}

function logNote(message) {
  // 16-digit Unix timestamp in microseconds
  const timestamp = String(Date.now() * 1000).padStart(16, '0');
  console.log(`${timestamp} ${message}`);
}

function waitUntil(targetTime) {
  const now = new Date();
  const waitMs = targetTime.getTime() - now.getTime();
  if (waitMs > 0) {
    sleep(waitMs / 1000);
  }
}

const testStartTime = new Date(Date.now());

export default function (data) {
  const vuId = __VU - 1;
  const headers = authHeaders();
  const createWorkbasket = data.workbaskets[0];
  const createClassification = data.classifications[0];

  // -------- PHASE 1: Create a new task --------
  waitUntil(testStartTime);
  if (__VU === 1) {
    logNote("Create task");
  }

  const taskPayload = JSON.stringify({
    name: `TestTask_${vuId}`,
    primaryObjRef: {
      company: 'test', system: 'test', systemInstance: 'test', type: 'test', value: `value_${vuId}`
    },
    workbasketSummary: workbasketSummary(createWorkbasket),
    classificationSummary: classificationSummary(createClassification),
  });

  const createRes = http.post(`${BASE_URL}/tasks`, taskPayload, { headers });
  check(createRes, { 'Created task': (r) => r.status === 201 || r.status === 200 });

  const task = createRes.json();
  const taskId = task.taskId;

  // -------- PHASE 2: Search for workbaskets with a specific permission (OPEN) --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS));
  if (__VU === 1) {
    logNote("Search for workbaskets");
  }
  const searchWorkbasketsRes = http.get(`${BASE_URL}/workbaskets?required-permission=OPEN`, { headers });
  check(searchWorkbasketsRes, { 'Search for workbaskets with a specific permission (OPEN)': (r) => r.status === 200 });

  // -------- PHASE 3: Open a workbasket --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 2));
  if (__VU === 1) {
    logNote("Open workbasket");
  }
  const workbasketToRead = randomItem(data.workbaskets).workbasketId;
  const readWorkbasketRes = http.get(`${BASE_URL}/workbaskets/${encodeURIComponent(workbasketToRead)}`, { headers });
  check(readWorkbasketRes, { 'Open a workbasket': (r) => r.status === 200 });

  // -------- PHASE 4: Retrieve the first 50 tasks from a workbasket --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 3));
  if (__VU === 1) {
    logNote("Retrieve tasks from workbasket");
  }
  const tasksFromWorkbasketRes = http.get(`${BASE_URL}/tasks?page=1&page-size=50&workbasket-id=${encodeURIComponent(workbasketToRead)}`, { headers });
  check(tasksFromWorkbasketRes, { 'Retrieve the first 50 tasks from a workbasket': (r) => r.status === 200 });

  // -------- PHASE 5: Read a single task --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 4));
  if (__VU === 1) {
    logNote("Read task");
  }
  const taskToRead = randomItem(data.taskIds);
  const readTaskRes = http.get(`${BASE_URL}/tasks/${encodeURIComponent(taskToRead)}`, { headers });
  check(readTaskRes, { 'Read a single task': (r) => r.status === 200 });

  // -------- PHASE 6: Edit a task --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 5));
  if (__VU === 1) {
    logNote("Edit task");
  }
  task.note = 'Updated by k6';
  const updateTaskRes = http.put(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}`, JSON.stringify(task), { headers });
  check(updateTaskRes, { 'Edit a task': (r) => r.status === 200 });

  // -------- PHASE 7: Transfer a task to another workbasket --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 6));
  if (__VU === 1) {
    logNote("Transfer task");
  }
  const newWbId = randomItem(data.workbaskets).workbasketId;
  const transferTaskRes = http.post(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/transfer/${encodeURIComponent(newWbId)}`, '{}', { headers });
  check(transferTaskRes, { 'Transfer a task to another workbasket': (r) => r.status === 200 });

  // -------- PHASE 8: Claim task --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 7));
  if (__VU === 1) {
    logNote("Claim task");
  }
  const claimTaskRes = http.post(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/claim`, null, { headers });
  check(claimTaskRes, { 'Claim task': (r) => r.status === 200 });

  // -------- PHASE 9: Retrieve comments for task --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 8));
  if (__VU === 1) {
    logNote("Retrieve comments");
  }
  const readCommentsRes = http.get(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/comments`, { headers });
  check(readCommentsRes, { 'Retrieve comments for task': (r) => r.status === 200 });

  // -------- PHASE 10: Add new comment to task --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 9));
  if (__VU === 1) {
    logNote("Add comment");
  }
  const commentPayload = JSON.stringify({
    taskId: taskId,
    textField: `Kommentar von VU ${vuId}`,
    creator: 'k6-script',
    creatorFullName: 'K6 Load Test',
  });
  const commentRes = http.post(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/comments`, commentPayload, { headers });
  check(commentRes, { 'Add new comment to task': (r) => r.status === 201 });
  const comment = commentRes.json();
  const commentId = comment.taskCommentId;

  // -------- PHASE 11: Delete comment --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 10));
  if (__VU === 1) {
    logNote("Delete comment");
  }
  const deleteCommentRes = http.del(`${BASE_URL}/tasks/comments/${encodeURIComponent(commentId)}`, null, { headers });
  check(deleteCommentRes, { 'Delete comment': (r) => r.status === 204 });
  
  // -------- PHASE 12: Complete task --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 11));
  if (__VU === 1) {
    logNote("Complete task");
  }
  const finishTaskRes = http.post(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/complete`, null, { headers });
  check(finishTaskRes, { 'Complete task': (r) => r.status === 200 });

  // -------- PHASE 13: Search for tasks by POR --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 12));
  if (__VU === 1) {
    logNote("Search for tasks by POR");
  }
  const porValue = task.primaryObjRef?.value ?? '';
  const searchByPorRes = http.get(`${BASE_URL}/tasks?por-type=${encodeURIComponent('Object Type')}&por-value=${porValue}`, { headers });
  check(searchByPorRes, { 'Search for tasks by POR': (r) => r.status === 200 });


  // -------- PHASE 14: Retrieve the task status report (by a single user) --------
  waitUntil(new Date(testStartTime.getTime() + PAUSE_MS * 13));
  if (__VU === 1) {
    logNote("Retrieve the task status report");
    const reportRes = http.get(`${BASE_URL}/monitor/task-status-report`, { headers });
    check(reportRes, { 'Retrieve the task status report': (r) => r.status === 200 });
  }
}
