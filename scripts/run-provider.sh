#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

JAR=$(find "$SCRIPT_DIR/server-provider/target" -name "*.jar" ! -name "*sources*" ! -name "*original*" 2>/dev/null | head -1)

if [ -z "$JAR" ]; then
  echo "JAR not found. Run ./build.sh first."
  exit 1
fi

echo "Starting server-provider (port 8080)..."
java -jar "$JAR"
