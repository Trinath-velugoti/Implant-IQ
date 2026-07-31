#!/bin/bash
echo "Setting up environment for Appium..."
export PATH=$PATH:$(cat $GITHUB_PATH)

echo "Installing APK..."
adb install -r "${APK_PATH}"

echo "Starting Appium Server..."
appium --log-level warn > /tmp/appium.log 2>&1 &

echo "Waiting for Appium..."
timeout 60 bash -c 'until curl -s http://localhost:4723/status; do sleep 2; done'

echo "Running WDIO Tests..."
cd app_e2e
node node_modules/@wdio/cli/bin/wdio.js run wdio.conf.js
