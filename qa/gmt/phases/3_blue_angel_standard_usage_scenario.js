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
import encoding from 'k6/encoding';

const HOST = __ENV.TARGET_HOSTNAME || 'app';
const PORT = __ENV.TARGET_PORT || '8080';
const BASE_URL = `http://${HOST}:${PORT}/kadai/api/v1`;
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

const username = 'admin';
const password = 'admin';

const credentials = `${username}:${password}`;
const encodedCredentials = encoding.b64encode(credentials);
const headers = {
  'Authorization': `Basic ${encodedCredentials}`,
  'Content-Type': 'application/json',
};

// --- Test Data --- //
const workbasketIds = [
  'AWB02XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'AWB03XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'AWB04XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'AWB05XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'AWB06XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'AWB07XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'AWB08XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'AWB09XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'AWB10XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'BWB01XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'BWB03XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'BWB04XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'BWB05XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'CWB01XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'CWB02XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'CWB03XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'CWB05XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
];

const taskIds = [
  'TKI:00006eee-a5f6-445e-aca5-30124cf4676d',
  'TKI:0001f6ff-6a74-48e4-91db-9a8a497d5ddc',
  'TKI:00033615-2659-44b1-90fe-f26a63bb0ad3',
  'TKI:000535ab-bff7-4a4d-8675-8a3185fe237d',
  'TKI:00066392-1c8a-4caf-8437-c34702b2a8c8',
  'TKI:00078502-c418-4575-b598-38b8bcae22af',
  'TKI:0008a262-aead-4f3a-9624-35318bbaf2fa',
  'TKI:00093e58-9587-44be-8256-a78fdc3adaeb',
  'TKI:00094728-4254-4acd-bbaf-290a5ccfc3a4',
  'TKI:00096c6c-2d34-4acf-9052-729236fbe7b3',
  'TKI:000a4cfa-a32d-450d-8cff-0295653827a5',
  'TKI:000c3738-214a-42ae-bc16-e3767cd8824a',
  'TKI:000c9995-aa11-4203-8d8d-8c105dc2364b',
  'TKI:000d2330-1fcd-466d-b897-b2b91a87a271',
  'TKI:000dd188-9e63-4ee5-b67c-490628374951',
  'TKI:000fb85a-051f-4d1a-9aa4-4119934cbf2d',
  'TKI:00105a95-0ee0-44ee-a47c-087f69f452cf',
  'TKI:0010c126-1cba-4477-b6d3-dff73baf2f10',
  'TKI:0012f53b-b2f9-4925-9267-44008f3c0356',
  'TKI:0013deea-dfd3-4935-b444-c4505b435bfe',
  'TKI:0018394c-57bc-4c6f-b420-c49c4d268d59',
  'TKI:00191618-d43f-4028-ae30-a3353039c1f4',
  'TKI:00199b9c-4dda-4822-a5fa-438c8a39eed5',
  'TKI:001ae459-af46-4770-ba3a-d6bf20d6273a',
  'TKI:001b0191-f109-4937-906b-b6be9517fe2d',
  'TKI:001b201e-6994-4716-8b82-f0b167930e73',
  'TKI:001ccfb8-7cb6-40fe-82be-1a3be22bb5c6',
  'TKI:001cf1e5-0f20-42fc-a490-3207fe3827e2',
  'TKI:001cf8ee-5012-45fb-9d8c-6eb97003d552',
  'TKI:001d2d7b-cf61-4d96-9e92-7198bee1bd6c',
  'TKI:001d783c-a5c9-42ed-bfde-64a8377ccc14',
  'TKI:001d91bf-be61-47e8-b604-9bbe0bc7ba38',
  'TKI:001e4cd8-2586-454f-b9fc-46ea9bc2b7d6',
  'TKI:001f853e-c128-46db-a8f9-4ebe4b8588df',
  'TKI:0020f6d4-863e-419a-b9c6-b158390cd0a1',
  'TKI:0021f849-979e-4354-a499-2f4457c6eb2f',
  'TKI:0022f87b-a14a-406a-9622-4fc0b8838e56',
  'TKI:0023e0da-a6c9-4182-8dff-5092f11484a0',
  'TKI:0025cffc-f62d-40ef-a69c-cc4d39adcd72',
  'TKI:00268545-1d00-4756-aff4-ec337b89133f',
  'TKI:0026a4d3-e32c-4607-ba1a-df586ed3c901',
  'TKI:00279a50-e9d7-467a-b858-9fb341e8dfb1',
  'TKI:0027bae4-d110-4c22-8742-fc79a7b12ad2',
  'TKI:0027bd31-d315-461f-b6bd-dc17e1d47022',
  'TKI:00289f35-0a7f-45be-95da-aa11176ce7b1',
  'TKI:00290d6d-c7d5-498a-aaed-6d34e2be113f',
  'TKI:00297bdb-d633-4edd-8885-aca076aac9e8',
  'TKI:0029d6f9-283a-4a65-b351-04ff63186253',
  'TKI:002d6273-43a7-44f6-a3c8-b458de4f9c27',
  'TKI:002dfe43-86b3-4a37-8eb7-1ad946d9c0d2',
  'TKI:002ea7c2-d87e-4413-bf06-26e0ccc83e38',
  'TKI:002ec47c-b391-4abf-b3bb-81b32ad5abd2',
  'TKI:002ed8c7-8fa0-42d9-b35f-42024146cbf0',
  'TKI:002f39cf-366e-4e28-a8c9-54cad1cb26c2',
  'TKI:002fb95a-6373-4cc2-8151-88e0d97c36e2',
  'TKI:00300318-caab-492d-9734-8f1566d4b204',
  'TKI:003009c3-d44f-4750-b453-50eb9d883272',
  'TKI:003171c1-31cf-4333-aeba-94c39f2c30b1',
  'TKI:00328fb0-f2b7-4642-9fd9-6e04ae3a183e',
  'TKI:00335ee7-169b-4d7f-b942-bba766f291e4',
  'TKI:003400f8-f964-47a5-ba0d-af0cd205a937',
  'TKI:003409b3-f3c8-45bd-a678-7a164bf60611',
  'TKI:00357343-1958-44b7-aa97-9bccfd9cd6ca',
  'TKI:003599c1-410f-44dc-8338-4a708103a695',
  'TKI:00378edd-710d-434f-9e85-edd9dfce5dda',
  'TKI:0039ad1d-04fa-4063-aadb-0d583b0d4fdb',
  'TKI:003a7644-f3fc-4201-80b5-952afda1d0e9',
  'TKI:003af20f-5b0c-4733-87c7-f1be7d04d660',
  'TKI:003b3aa9-eba8-4d81-981b-97e405bf0b9f',
  'TKI:003b52a8-bd36-45af-80ea-283182316ecf',
  'TKI:003be012-04ee-469b-826b-18ff4de88d5d',
  'TKI:003c225e-2932-4153-8217-d24bb660a627',
  'TKI:003cf90d-4801-47b4-9cca-d29fb47b4220',
  'TKI:003dc110-b785-45dc-a52e-55dbfd28d29a',
  'TKI:003e454d-d767-4e70-af89-25ba2fe48187',
  'TKI:003ebc6e-6276-4e3c-95c9-69b5ac447d37',
  'TKI:003f6a55-6cf8-4a34-9308-ded1bc63b015',
  'TKI:003fdfd1-be79-4161-977b-611719f2ac38',
  'TKI:004094a3-7957-43d5-a20d-451fa8b97a7b',
  'TKI:00411c65-4ebe-4c72-81e4-4ca4ca538ed8',
  'TKI:00415a44-5ccd-4da9-b260-8ff5c824a570',
  'TKI:0043a2e0-d1ce-432f-af7e-6c734ad81d47',
  'TKI:0043a429-afa5-43b7-b194-3d05bec5bcef',
  'TKI:0044a889-98ec-4372-84ea-9a34fa4b3eeb',
  'TKI:0044b8de-dff8-4fca-8203-1980f99fe7c8',
  'TKI:00452ec3-cc4a-4159-be90-d50304092ef5',
  'TKI:0045aa4b-f7c2-413b-8d4a-231c28daf65b',
  'TKI:00460e29-fc8d-4cc3-a781-a9edfc83c02c',
  'TKI:0047b093-10d3-4d18-a9c7-70f7a57abac7',
  'TKI:0047c80a-b48c-4ee9-908a-59075f8fc02f',
  'TKI:00484cc1-71c0-49da-b847-a46f9363d8d1',
  'TKI:0049f482-7c8e-426d-8b4a-7bc8754b3b03',
  'TKI:004a2c42-e91f-4fc5-9a0a-25f38e471b56',
  'TKI:004ab344-e4f7-44f0-95b3-a85f369f5a23',
  'TKI:004b07d1-7ef4-452a-bd13-cd7c6c082818',
  'TKI:004b720b-3465-4feb-a8c3-20178644dea1',
  'TKI:004bfe19-6beb-4518-a1ac-70efdda8a1d8',
  'TKI:004c531a-c207-45d0-baf9-792e01d9ee64',
  'TKI:004d52ca-c589-4f20-87fd-16489d00a907',
  'TKI:004dccad-9bc4-4bfb-aee1-449aa7d5a1fb'
];


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

export default function () {
  const vuId = __VU - 1;

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
    workbasketSummary: {
      workbasketId: 'CWB05XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
      key: 'CWB05',
      name: 'CWB05XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
      domain: 'DOMAIN_C',
      type: 'PERSONAL',
      description: 'CWB05XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
      owner: 'CU05',
      markedForDeletion: false,
    },
    classificationSummary: {
      classificationId: 'CLI:4402a936-8608-4d3e-b36c-3a6004547398',
      key: 'AUTOMATIC',
      category: 'AUTOMATIC',
      domain: 'DOMAIN_A',
      name: '',
      priority: 0,
      serviceLevel: 'P0D',
      type: 'TASK',
      custom1: 'ANR, RVNR, VNR, KOLVNR',
    },
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
  const workbasketToRead = workbasketIds[Math.floor(Math.random() * workbasketIds.length)];
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
  const taskToRead = taskIds[Math.floor(Math.random() * taskIds.length)];
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
  const newWbId = workbasketIds[Math.floor(Math.random() * workbasketIds.length)];
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
