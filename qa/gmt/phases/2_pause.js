import { sleep } from 'k6';

function logNote(message) {
  const timestamp = String(Date.now() * 1000).padStart(16, '0');
  console.log(`${timestamp} ${message}`);
}

export default function () {
  const seconds = Number(__ENV.PAUSE_SECONDS || '30');
  logNote(`Warte ${seconds}s vor Messung`);
  sleep(seconds);
}
