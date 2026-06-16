import http from 'k6/http';
import encoding from 'k6/encoding';

const HOST = __ENV.TARGET_HOSTNAME || 'app';
const PORT = __ENV.TARGET_PORT || '8080';
const BASE_URL = `http://${HOST}:${PORT}/kadai/api/v1`;

export const options = {
  scenarios: {
    default: {
      executor: 'per-vu-iterations',
      vus: 10,
      iterations: 15,  // each VU runs 15 times
    },
  },
};

const username = 'admin';
const password = 'admin';
const encodedCredentials = encoding.b64encode(`${username}:${password}`);
const headers = {
  'Authorization': `Basic ${encodedCredentials}`,
  'Content-Type': 'application/json',
};

const workbasketIds = [
  'AWB01XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'CWB04XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
  'BWB02XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
];

const taskIds = [
  'TKI:004dd860-2e04-43b0-a6a5-89bc8ea76f04',
  'TKI:004eeb0a-a390-4279-8d90-3358df682092',
  'TKI:004f3d6b-0500-4839-8905-48615769df31',
  'TKI:004ff124-becf-4610-8a45-f799263ebb2a',
  'TKI:00507030-46c1-4163-a22b-48fbe74eccc7',
  'TKI:0050ab43-1769-4e24-9aed-4eb9a7945263',
  'TKI:00516f90-997b-47fe-8e77-28678c72bd32',
];

export default function () {
  // Create a new task
  const createRes = http.post(`${BASE_URL}/tasks`, JSON.stringify({
    name: 'WarmupTask',
    primaryObjRef: { company: 'test', system: 'test', systemInstance: 'test', type: 'test', value: 'test' },
    workbasketSummary: { workbasketId: workbasketIds[0], key: 'WB', domain: 'DOMAIN_C', type: 'PERSONAL' },
    classificationSummary: { classificationId: 'CLI:4402a936-8608-4d3e-b36c-3a6004547398', key: 'AUTOMATIC', domain: 'DOMAIN_A', type: 'TASK' },
  }), { headers });
  
  const task = createRes.json();
  const taskId = task.taskId;

  // Search for workbaskets with a specific permission (OPEN)
  http.get(`${BASE_URL}/workbaskets?required-permission=OPEN`, { headers });

  // Open a workbasket
  const workbasketToRead = workbasketIds[Math.floor(Math.random() * workbasketIds.length)];
  http.get(`${BASE_URL}/workbaskets/${encodeURIComponent(workbasketToRead)}`, { headers });

  // Retrieve the first 50 tasks from a workbasket
  http.get(`${BASE_URL}/tasks?page=1&page-size=50&workbasket-id=${encodeURIComponent(workbasketToRead)}`, { headers });

  // Read a single task
  const taskToRead = taskIds[Math.floor(Math.random() * taskIds.length)];
  http.get(`${BASE_URL}/tasks/${encodeURIComponent(taskToRead)}`, { headers });

   // Edit a task
  task.note = 'warm up';
  http.put(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}`, JSON.stringify(task), { headers });

  // Transfer a task to another workbasket
  const newWbId = workbasketIds[Math.floor(Math.random() * workbasketIds.length)];
  http.post(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/transfer/${encodeURIComponent(newWbId)}`, '{}', { headers });

  // Assign task (Claim)
  http.post(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/claim`, null, { headers });

  // Retrieve comments for task
  http.get(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/comments`, { headers });

  // Add new comment to task
  const commentRes = http.post(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/comments`, JSON.stringify({ textField: 'warmup' }), { headers });
  const commentId = commentRes.json().taskCommentId;

  // Delete comment
  if (commentId) {
    http.del(`${BASE_URL}/tasks/comments/${encodeURIComponent(commentId)}`, null, { headers });
  }

  // Complete task
  http.post(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}/complete`, null, { headers });

  // Search for tasks by POR
  http.get(`${BASE_URL}/tasks?por-type=${encodeURIComponent('Object Type')}&por-value=test`, { headers });

  // Retrieve the task status report (Monitoring)
  http.get(`${BASE_URL}/monitor/task-status-report`, { headers });
}
