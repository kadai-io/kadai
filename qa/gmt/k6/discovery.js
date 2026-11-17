import http from 'k6/http';
import encoding from 'k6/encoding';
import { fail } from 'k6';

const HOST = __ENV.TARGET_HOSTNAME || 'app';
const PORT = __ENV.TARGET_PORT || '8080';
const BASE_URL = `http://${HOST}:${PORT}/kadai/api/v1`;

const PAGE_SIZE = 100;
const DEFAULT_POOL_OFFSET = 0;
const DEFAULT_POOL_SIZE = 50;

const encodedCredentials = encoding.b64encode('admin:admin');
const headers = {
  'Authorization': `Basic ${encodedCredentials}`,
  'Content-Type': 'application/json',
};

export function baseUrl() {
  return BASE_URL;
}

export function authHeaders() {
  return headers;
}

export function poolOffset() {
  return Number(__ENV.TASK_POOL_OFFSET || String(DEFAULT_POOL_OFFSET));
}

export function poolSize() {
  return Number(__ENV.TASK_POOL_SIZE || String(DEFAULT_POOL_SIZE));
}

export function discoverWorkbaskets() {
  const res = http.get(`${BASE_URL}/workbaskets?required-permission=OPEN`, { headers });
  if (res.status !== 200) {
    fail(`Discovering workbaskets failed: HTTP ${res.status} - ${res.body}`);
  }
  return res.json().workbaskets || [];
}

export function discoverClassifications() {
  const res = http.get(`${BASE_URL}/classifications?page=1&page-size=${PAGE_SIZE}`, { headers });
  if (res.status !== 200) {
    fail(`Discovering classifications failed: HTTP ${res.status} - ${res.body}`);
  }
  const classifications = res.json().classifications || [];
  return classifications.filter((classification) => classification.type === 'TASK');
}

// Returns a deterministic, gap-free slice of tasks. Sorting ascending by CREATED
// keeps pre-existing tasks in stable pages: tasks created by the flow get the
// current timestamp and are appended at the end, so warm-up and measurement can
// use disjoint slices.
export function discoverTaskIds(offset = poolOffset(), size = poolSize()) {
  const taskIds = [];
  let page = Math.floor(offset / PAGE_SIZE) + 1;
  let skip = offset % PAGE_SIZE;

  while (taskIds.length < size) {
    const res = http.get(
      `${BASE_URL}/tasks?page=${page}&page-size=${PAGE_SIZE}` +
        '&sort-by=CREATED&order=ASCENDING&sort-by=TASK_ID&order=ASCENDING',
      { headers },
    );
    if (res.status !== 200) {
      fail(`Discovering tasks failed: HTTP ${res.status} - ${res.body}`);
    }
    const batch = res.json().tasks || [];
    if (batch.length === 0) {
      break;
    }
    for (let i = skip; i < batch.length && taskIds.length < size; i++) {
      taskIds.push(batch[i].taskId);
    }
    skip = 0;
    page += 1;
  }

  if (taskIds.length < size) {
    fail(`Only found ${taskIds.length} of ${size} requested tasks (offset ${offset})`);
  }
  return taskIds;
}

export function discover(offset = poolOffset(), size = poolSize()) {
  return {
    workbaskets: discoverWorkbaskets(),
    classifications: discoverClassifications(),
    taskIds: discoverTaskIds(offset, size),
  };
}

export function randomItem(items) {
  return items[Math.floor(Math.random() * items.length)];
}
