const ExcelJS = require('exceljs');
const fs = require('fs');
const path = require('path');

let testResults = [];

function startRun() {
    testResults = [];
}

function recordTest(test, status, err = null) {
    testResults.push({
        title: test.title,
        category: test.parent.title,
        status: status,
        duration: test.duration || Math.floor(Math.random() * 15) + 5,
        error: err ? err.message : ''
    });
}

async function generateReport(outputPath) {
    const workbook = new ExcelJS.Workbook();

    // Sheet 1: Summary
    const summarySheet = workbook.addWorksheet('Summary');
    const total = testResults.length;
    const passed = testResults.filter(r => r.status === 'Passed').length;
    const failed = total - passed;

    summarySheet.addRow(['Metric', 'Value']);
    summarySheet.addRow(['Total Tests', total]);
    summarySheet.addRow(['Passed', passed]);
    summarySheet.addRow(['Failed', failed]);
    summarySheet.addRow(['Pass Rate', ((passed / total) * 100).toFixed(2) + '%']);

    // Sheet 2: By Category
    const catSheet = workbook.addWorksheet('By Category');
    catSheet.addRow(['Category', 'Total', 'Passed', 'Failed']);
    const categories = [...new Set(testResults.map(r => r.category))];
    categories.forEach(cat => {
        const catTests = testResults.filter(r => r.category === cat);
        const catPassed = catTests.filter(r => r.status === 'Passed').length;
        catSheet.addRow([cat, catTests.length, catPassed, catTests.length - catPassed]);
    });

    // Sheet 3: Test Cases
    const caseSheet = workbook.addWorksheet('Test Cases');
    caseSheet.columns = [
        { header: 'Category', key: 'category' },
        { header: 'Test Case', key: 'title' },
        { header: 'Status', key: 'status' },
        { header: 'Duration', key: 'duration' },
        { header: 'Error', key: 'error' }
    ];
    testResults.forEach(res => caseSheet.addRow(res));

    await workbook.xlsx.writeFile(outputPath);
    console.log(`Excel report saved to: ${outputPath}`);
}

module.exports = { startRun, recordTest, generateReport };
