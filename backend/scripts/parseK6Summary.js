const fs = require('fs');

function getMetricValue(metricObj, key) {
    if (!metricObj) return 'N/A';
    if (metricObj.values && metricObj.values[key] !== undefined) {
        return metricObj.values[key];
    }
    if (metricObj[key] !== undefined) {
        return metricObj[key];
    }
    return 'N/A';
}

try {
    const summaryData = JSON.parse(fs.readFileSync('summary.json', 'utf8'));
    const metrics = summaryData.metrics;

    const rps = getMetricValue(metrics.http_reqs, 'rate');
    const totalRequests = getMetricValue(metrics.http_reqs, 'count');
    const avgResponseTime = getMetricValue(metrics.http_req_duration, 'avg');
    const minResponseTime = getMetricValue(metrics.http_req_duration, 'min');
    const maxResponseTime = getMetricValue(metrics.http_req_duration, 'max');
    const p95ResponseTime = getMetricValue(metrics.http_req_duration, 'p(95)');
    const failureRate = getMetricValue(metrics.http_req_failed, 'rate');
    const checkPassRate = getMetricValue(metrics.checks, 'rate');

    const markdownSummary = `
## 📈 API Load Test Results

| Metric | Value |
|--------|-------|
| **Throughput (RPS)** | ${typeof rps === 'number' ? rps.toFixed(2) : rps} req/sec |
| **Total Requests** | ${totalRequests} |
| **Average Response Time** | ${typeof avgResponseTime === 'number' ? avgResponseTime.toFixed(2) : avgResponseTime} ms |
| **Min Response Time** | ${typeof minResponseTime === 'number' ? minResponseTime.toFixed(2) : minResponseTime} ms |
| **Max Response Time** | ${typeof maxResponseTime === 'number' ? maxResponseTime.toFixed(2) : maxResponseTime} ms |
| **p(95) Response Time** | ${typeof p95ResponseTime === 'number' ? p95ResponseTime.toFixed(2) : p95ResponseTime} ms |
| **Failure Rate** | ${typeof failureRate === 'number' ? (failureRate * 100).toFixed(2) : failureRate}% |
| **Checks Pass Rate** | ${typeof checkPassRate === 'number' ? (checkPassRate * 100).toFixed(2) : checkPassRate}% |

> **Thresholds:** Failures < 5%, p(95) < 1500ms
`;

    if (process.env.GITHUB_STEP_SUMMARY) {
        fs.appendFileSync(process.env.GITHUB_STEP_SUMMARY, markdownSummary);
    } else {
        console.log(markdownSummary);
    }

} catch (error) {
    console.error('Error parsing k6 summary:', error.message);
    process.exit(1);
}
