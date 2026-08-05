#!/bin/bash
echo "Setting up environment for Appium..."
export PATH=$PATH:$(cat $GITHUB_PATH)

echo "Waiting for device to be ready..."
adb wait-for-device
while [ "`adb shell getprop sys.boot_completed | tr -d '\r'`" != "1" ] ; do
  echo "Waiting for system boot..."
  sleep 5
done

echo "System boot completed. Waiting for package manager..."
until adb shell pm list packages > /dev/null 2>&1; do
  echo "Waiting for package manager service..."
  sleep 5
done

# Safety buffer for system stability
sleep 20

echo "Installing APK..."
# Retry install to avoid 'Broken pipe' errors
adb install -r "${APK_PATH}" || (sleep 10 && adb install -r "${APK_PATH}")

echo "Starting Appium Server..."
appium --log-level warn --base-path / > /tmp/appium.log 2>&1 &

echo "Waiting for Appium..."
timeout 60 bash -c 'until curl -s http://localhost:4723/status; do sleep 2; done'

echo "Running WDIO Tests..."
cd app_e2e
node node_modules/@wdio/cli/bin/wdio.js run wdio.conf.js
