import { textSummary } from "https://jslib.k6.io/k6-summary/0.1.0/index.js";

function millisecondsToMicroseconds(value) {
  return Math.round(value * 1000);
}

export function handleSummary(data) {
    const timestamp = millisecondsToMicroseconds(Date.now())

    const duration = data.metrics.http_req_duration?.values ?? {};
    const throughput = data.metrics.http_reqs?.values?.rate ?? 0;

    const linesWithGmtCustomMetrics = [
        `${timestamp} http_req_duration_avg ${millisecondsToMicroseconds(duration.avg)}`,
        `${timestamp} http_req_duration_min ${millisecondsToMicroseconds(duration.min)}`,
        `${timestamp} http_req_duration_max ${millisecondsToMicroseconds(duration.max)}`,
        `${timestamp} http_req_duration_p95 ${millisecondsToMicroseconds(duration["p(95)"])}`,
        `${timestamp} http_reqs_per_second  ${Math.round(throughput)}`,
    ].join("\n");

    return {
        stdout: textSummary(data, { indent: " ", enableColors: false }) +
            "\n\nGMT Custom Metrics:\n" + linesWithGmtCustomMetrics + "\n",
    };
}
