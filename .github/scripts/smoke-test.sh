#!/usr/bin/env bash
set -euo pipefail

APK=$(find artifacts/ -name "*.apk" -not -name "*androidTest*" | head -1)
if [ -z "$APK" ]; then
  echo "::error::No release APK found in artifact. Contents:"
  find artifacts/ -type f | sort
  exit 1
fi

echo "Installing $APK"
adb install "$APK"

adb logcat -c
adb shell am start -n io.shellify.app/io.shellify.app.presentation.MainActivity
sleep 8

PID=$(adb shell pidof io.shellify.app || true)
if [ -z "$PID" ]; then
  echo "::error::App process not found — likely crashed on launch"
  adb logcat -d -s AndroidRuntime:E | tail -50
  exit 1
fi

if adb logcat -d | grep -q "FATAL EXCEPTION"; then
  echo "::error::FATAL EXCEPTION detected in logcat"
  adb logcat -d -s AndroidRuntime:E | tail -50
  exit 1
fi

echo "Smoke test passed — app is running with PID $PID"
