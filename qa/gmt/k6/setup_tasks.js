import http from 'k6/http';
import encoding from 'k6/encoding';
import { fail } from 'k6';

const HOST = __ENV.TARGET_HOSTNAME || 'app';
const PORT = __ENV.TARGET_PORT || '8080';
const BASE_URL = `http://${HOST}:${PORT}/kadai/api/v1`;

const SEED_TASKS = Number(__ENV.SEED_TASKS || '100000');
const SEED_TIMEOUT = __ENV.SEED_TIMEOUT || '10m';

export const options = {
  scenarios: {
    seed: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 1,
    },
  },
};

export default function () {
  const encodedCredentials = encoding.b64encode('admin:admin');
  const headers = {
    'Authorization': `Basic ${encodedCredentials}`,
    'Content-Type': 'application/json',
  };

  const res = http.post(`${BASE_URL}/gmt/tasks`, JSON.stringify({ taskCount: SEED_TASKS }), {
    headers,
    timeout: SEED_TIMEOUT,
  });

  if (res.status !== 200) {
    fail(`Seeding ${SEED_TASKS} tasks failed: HTTP ${res.status} - ${res.body}`);
  }

  console.log(`Seeded ${SEED_TASKS} tasks (HTTP ${res.status})`);
}
