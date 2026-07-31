const { recordTest, generateReport, startRun } = require('./utils/xlsxReporter');
const { generate: generateHtml } = require('./utils/generateHtmlReport');
const path = require('path');
const fs = require('fs');

exports.config = {
    runner: 'local',
    specs: ['./tests/**/*.test.js'],
    maxInstances: 1,
    capabilities: [{
        platformName: 'Android',
        'appium:automationName': 'UiAutomator2',
        'appium:deviceName': 'Android Emulator',
        'appium:app': process.env.APK_PATH || '../mobile_app/build/outputs/apk/debug/app-debug.apk',
        'appium:noReset': true,
        'appium:fullReset': false,
        'appium:autoGrantPermissions': true
    }],
    logLevel: 'warn',
    bail: 0,
    baseUrl: 'http://localhost',
    waitforTimeout: 10000,
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    services: ['appium'],
    framework: 'mocha',
    reporters: ['spec'],
    mochaOpts: {
        ui: 'bdd',
        timeout: 600000
    },
    onPrepare: function () {
        startRun();
    },
    afterTest: function (test, context, { error, result, duration, passed, retries }) {
        recordTest(test, passed ? 'Passed' : 'Failed', error);
    },
    onComplete: async function () {
        const excelPath = path.join(__dirname, '..', 'Test_Results', 'Excel', 'android-report.xlsx');
        const htmlPath = path.join(__dirname, '..', 'Test_Results', 'HTML', 'android-report.html');

        if (!fs.existsSync(path.dirname(excelPath))) fs.mkdirSync(path.dirname(excelPath), { recursive: true });
        if (!fs.existsSync(path.dirname(htmlPath))) fs.mkdirSync(path.dirname(htmlPath), { recursive: true });

        await generateReport(excelPath);
        await generateHtml(excelPath, htmlPath);
    }
}
