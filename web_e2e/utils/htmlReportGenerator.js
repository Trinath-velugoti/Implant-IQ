const fs = require('fs');
const path = require('path');

module.exports = {
    generateHTML: function (results, summary) {
        const total = results.length;
        const passed = results.filter(r => r.status === 'passed').length;
        const failed = total - passed;
        const passRate = ((passed / total) * 100).toFixed(1);

        const htmlContent = `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImplantIQ Web E2E Execution Report</title>
    <style>
        :root { --bg: #051125; --surface: #0a1b33; --accent: #18D7FF; --success: #18E0A1; --error: #FF5A7A; --text: #FFFFFF; --text-sec: #8F9BB3; }
        body { background: var(--bg); color: var(--text); font-family: 'Inter', sans-serif; margin: 0; padding: 2rem; }
        .container { max-width: 1200px; margin: 0 auto; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; border-bottom: 1px solid #1B4F9C; padding-bottom: 1rem; }
        .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem; }
        .stat-card { background: var(--surface); padding: 1.5rem; border-radius: 12px; border: 1px solid #1B4F9C; text-align: center; }
        .stat-value { font-size: 2.5rem; font-weight: 800; margin: 0.5rem 0; }
        .stat-label { color: var(--text-sec); font-size: 0.8rem; text-transform: uppercase; letter-spacing: 1px; }
        .table-container { background: var(--surface); border-radius: 12px; border: 1px solid #1B4F9C; overflow: hidden; }
        table { width: 100%; border-collapse: collapse; }
        th { background: #0E2D54; text-align: left; padding: 1rem; color: var(--accent); }
        td { padding: 1rem; border-bottom: 1px solid #1B4F9C; font-size: 0.9rem; }
        .status-passed { color: var(--success); font-weight: 700; }
        .status-failed { color: var(--error); font-weight: 700; }
        .category-badge { background: #1B4F9C; padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🛡️ ImplantIQ Automation Report</h1>
            <p style="color: var(--text-sec)">Execution Time: ${new Date().toLocaleString()}</p>
        </div>

        <div class="stats-grid">
            <div class="stat-card"><div class="stat-label">Total Tests</div><div class="stat-value">${total}</div></div>
            <div class="stat-card"><div class="stat-label">Passed</div><div class="stat-value" style="color: var(--success)">${passed}</div></div>
            <div class="stat-card"><div class="stat-label">Failed</div><div class="stat-value" style="color: var(--error)">${failed}</div></div>
            <div class="stat-card"><div class="stat-label">Pass Rate</div><div class="stat-value" style="color: var(--accent)">${passRate}%</div></div>
        </div>

        <h2>Category Breakdown</h2>
        <div class="table-container">
            <table>
                <thead>
                    <tr><th>Category</th><th>Total</th><th>Passed</th><th>Failed</th><th>Rate</th></tr>
                </thead>
                <tbody>
                    ${Object.keys(summary).map(cat => `
                        <tr>
                            <td><strong>${cat}</strong></td>
                            <td>${summary[cat].total}</td>
                            <td class="status-passed">${summary[cat].passed}</td>
                            <td class="status-failed">${summary[cat].failed}</td>
                            <td>${((summary[cat].passed / summary[cat].total) * 100).toFixed(1)}%</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>`;

        const outputPath = path.join(process.cwd(), 'Test_Results', 'HTML', 'execution-report.html');
        const dir = path.dirname(outputPath);
        if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
        fs.writeFileSync(outputPath, htmlContent);
        console.log(`HTML report generated: ${outputPath}`);
    }
};
