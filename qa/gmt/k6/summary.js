import { textSummary } from "https://jslib.k6.io/k6-summary/0.1.0/index.js";

export function handleSummary(data) {
    const timestamp = Date.now() * 1000; // milliseconds → microseconds

    const duration = data.metrics.http_req_duration?.values ?? {};
    const throughput = data.metrics.http_reqs?.values?.rate ?? 0;

    const linesWithGmtCustomMetrics = [
        `${timestamp} http_req_duration_avg ${Math.round(duration.avg)}`,
        `${timestamp} http_req_duration_min ${Math.round(duration.min)}`,
        `${timestamp} http_req_duration_max ${Math.round(duration.max)}`,
        `${timestamp} http_req_duration_p95 ${Math.round(duration["p(95)"])}`,
        `${timestamp} http_reqs_per_second  ${Math.round(throughput)}`,
    ].join("\n");

    return {
        stdout: textSummary(data, { indent: " ", enableColors: false }) +
            "\n\nGMT Custom Metrics:\n" + linesWithGmtCustomMetrics + "\n",
    };
}
