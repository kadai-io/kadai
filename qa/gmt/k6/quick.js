import http from 'k6/http';
import { authHeaders, baseUrl, discover, randomItem } from './discovery.js';

// Use custom summary
export { handleSummary } from "./summary.js";

const BASE_URL = baseUrl();

export const options = {
  scenarios: {
    default: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
    },
  },
};

export function setup() {
  return discover();
}

export default function (data) {
  const headers = authHeaders();
  const createWorkbasket = randomItem(data.workbaskets);
  const classification = randomItem(data.classifications);

  // Create a new task
  const createRes = http.post(`${BASE_URL}/tasks`, JSON.stringify({
    name: 'WarmupTask',
    primaryObjRef: { company: 'test', system: 'test', systemInstance: 'test', type: 'test', value: 'test' },
    workbasketSummary: {
      workbasketId: createWorkbasket.workbasketId,
      key: createWorkbasket.key,
      domain: createWorkbasket.domain,
      type: createWorkbasket.type,
    },
    classificationSummary: {
      classificationId: classification.classificationId,
      key: classification.key,
      domain: classification.domain,
      type: classification.type,
    },
  }), { headers });

  const task = createRes.json();
  const taskId = task.taskId;

  // Search for workbaskets with a specific permission (OPEN)
  http.get(`${BASE_URL}/workbaskets?required-permission=OPEN`, { headers });

  // Open a workbasket
  const workbasketToRead = randomItem(data.workbaskets);
  http.get(`${BASE_URL}/workbaskets/${encodeURIComponent(workbasketToRead.workbasketId)}`, { headers });

  // Retrieve the first 50 tasks from a workbasket
  http.get(`${BASE_URL}/tasks?page=1&page-size=50&workbasket-id=${encodeURIComponent(workbasketToRead.workbasketId)}`, { headers });

  // Read a single task
  const taskToRead = randomItem(data.taskIds);
  http.get(`${BASE_URL}/tasks/${encodeURIComponent(taskToRead)}`, { headers });

   // Edit a task
  task.note = 'warm up';
  http.put(`${BASE_URL}/tasks/${encodeURIComponent(taskId)}`, JSON.stringify(task), { headers });

  // Transfer a task to another workbasket
  const newWbId = randomItem(data.workbaskets).workbasketId;
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
