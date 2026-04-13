#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
GRADLEW="$PROJECT_ROOT/gradlew"
PACKAGE_NAME="com.example.weedx"

cd "$PROJECT_ROOT"

"$GRADLEW" build
if [[ ! -f "$APK_PATH" ]]; then
  echo "ERROR: APK not found at $APK_PATH"
  exit 1
fi
echo "Uninstalling existing app..."
adb uninstall "$PACKAGE_NAME" || true

echo "Installing APK: $APK_PATH"
adb install -r "$APK_PATH"

echo "Launching app via monkey..."
adb shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1

echo "Clearing logcat and streaming filtered logs..."
adb logcat -c
adb logcat -v time | grep --line-buffered -E "$PACKAGE_NAME|AndroidRuntime|FATAL EXCEPTION|OkHttp|Retrofit|System.err"
