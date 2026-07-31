const fs = require('fs');
const path = require('path');
const ExcelJS = require('exceljs');

async function publish() {
    const excelPath = path.join(__dirname, '..', '..', 'Test_Results', 'Excel', 'android-report.xlsx');
    const workbook = new ExcelJS.Workbook();
    await workbook.xlsx.readFile(excelPath);

    const summarySheet = workbook.getWorksheet('Summary');
    const total = summarySheet.getCell('B2').value;
    const passed = summarySheet.getCell('B3').value;
    const passRate = summarySheet.getCell('B5').value;

    const summary = `
## 📱 Android E2E Test Results
- **Total Tests:** ${total}
- **Passed:** ${passed}
- **Pass Rate:** ${passRate}
- ✅ Android Automation Suite completed.
`;

    if (process.env.GITHUB_STEP_SUMMARY) {
        fs.appendFileSync(process.env.GITHUB_STEP_SUMMARY, summary);
    }
}

publish();
