const fs = require('fs');
const path = require('path');
const ExcelJS = require('exceljs');

async function generate(excelPath, outputPath) {
    const workbook = new ExcelJS.Workbook();
    await workbook.xlsx.readFile(excelPath);

    const summarySheet = workbook.getWorksheet('Summary');
    const total = summarySheet.getCell('B2').value;
    const passed = summarySheet.getCell('B3').value;
    const failed = summarySheet.getCell('B4').value;
    const passRate = summarySheet.getCell('B5').value;

    const htmlContent = `
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>ImplantIQ Android Automation Report</title>
        <style>
            body { font-family: 'Inter', sans-serif; background-color: #0A1628; color: #fff; margin: 0; padding: 20px; }
            .header { text-align: center; margin-bottom: 30px; }
            .summary-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 30px; }
            .card { background: #0D1F3C; padding: 20px; border-radius: 10px; text-align: center; border: 1px solid #1B4F9C; }
            .value { font-size: 24px; font-weight: bold; }
            .label { color: #8F9BB3; font-size: 14px; }
            .passed { color: #18E0A1; }
            .failed { color: #FF5A7A; }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>ImplantIQ Android E2E Report</h1>
            <p>Executed via Appium in CI - ${new Date().toLocaleString()}</p>
        </div>
        <div class="summary-cards">
            <div class="card"><div class="value">${total}</div><div class="label">Total Tests</div></div>
            <div class="card"><div class="value passed">${passed}</div><div class="label">Passed</div></div>
            <div class="card"><div class="value failed">${failed}</div><div class="label">Failed</div></div>
            <div class="card"><div class="value">${passRate}</div><div class="label">Pass Rate</div></div>
        </div>
    </body>
    </html>
    `;

    fs.writeFileSync(outputPath, htmlContent);
}

module.exports = { generate };
