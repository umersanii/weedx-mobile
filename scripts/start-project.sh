#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

MODE="${1:-all}"

print_usage() {
    echo "Usage: bash scripts/start-project.sh [all|backend|app]"
    echo ""
    echo "Modes:"
    echo "  all      Deploy/start backend and build Android app (default)"
    echo "  backend  Deploy/start backend only"
    echo "  app      Build Android app only"
}

start_backend() {
    echo "========================================"
    echo "Starting WeedX backend"
    echo "========================================"
    mkdir -p "$PROJECT_ROOT/logs"

    if [ ! -f "$PROJECT_ROOT/scripts/deploy-backend.sh" ]; then
        echo "❌ Missing deploy script: scripts/deploy-backend.sh"
        exit 1
    fi

    bash "$PROJECT_ROOT/scripts/deploy-backend.sh"

    echo ""
    echo "Starting MQTT subscriber (background)..."
    pkill -f "php.*subscriber.php" 2>/dev/null || true
    nohup bash -c "cd '$PROJECT_ROOT/xampp/htdocs/backend' && php mqtt/subscriber.php" \
        >> "$PROJECT_ROOT/logs/mqtt-subscriber.log" 2>&1 &
    echo "✅ MQTT subscriber started (PID $!)"

    echo ""
    echo "✅ Backend ready"
    echo "   URL: http://localhost/weedx-backend/"
}

start_app() {
    echo "========================================"
    echo "Building WeedX Android app"
    echo "========================================"

    if [ ! -f "$PROJECT_ROOT/gradlew" ]; then
        echo "❌ gradlew not found in project root"
        exit 1
    fi

    cd "$PROJECT_ROOT"
    chmod +x ./gradlew
    ./gradlew assembleDebug

    echo ""
    echo "✅ Android debug build complete"
    echo "   APK: app/build/outputs/apk/debug/app-debug.apk"
}

if [ ! -f "$PROJECT_ROOT/settings.gradle.kts" ]; then
    echo "❌ Please run this script from inside the weedx-mobile project"
    exit 1
fi

case "$MODE" in
    all)
        start_backend
        echo ""
        start_app
        ;;
    backend)
        start_backend
        ;;
    app)
        start_app
        ;;
    -h|--help|help)
        print_usage
        ;;
    *)
        echo "❌ Invalid mode: $MODE"
        echo ""
        print_usage
        exit 1
        ;;
esac

echo ""
echo "🎉 Done"
