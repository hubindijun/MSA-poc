#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== Step 1/3: Starting Nacos ==="
"$SCRIPT_DIR/scripts/run-nacos.sh"

echo ""
echo "Waiting for Nacos to be ready..."
for i in {1..30}; do
  if curl -s http://localhost:8848/nacos/v1/console/health/readiness > /dev/null 2>&1; then
    echo "Nacos is ready."
    break
  fi
  sleep 2
done

echo ""
echo "=== Step 2/3: Starting server-provider (port 8080) in background ==="
PROVIDER_JAR=$(find "$SCRIPT_DIR/server-provider/target" -name "*.jar" ! -name "*sources*" ! -name "*original*" 2>/dev/null | head -1)
if [ -z "$PROVIDER_JAR" ]; then
  echo "Provider JAR not found. Run ./build.sh first."
  exit 1
fi

nohup java -jar "$PROVIDER_JAR" > /tmp/server-provider.log 2>&1 &
PROVIDER_PID=$!
echo "server-provider PID: $PROVIDER_PID (logs: /tmp/server-provider.log)"

echo "Waiting for server-provider to start..."
for i in {1..60}; do
  if curl -s http://localhost:8080/provider/query > /dev/null 2>&1; then
    echo "server-provider is ready."
    break
  fi
  sleep 2
done

echo ""
echo "=== Step 3/3: Starting server-consumer (port 8081) in foreground ==="
echo "Tip: Press Ctrl+C to stop server-consumer. Nacos and server-provider keep running."
echo ""
CONSUMER_JAR=$(find "$SCRIPT_DIR/server-consumer/target" -name "*.jar" ! -name "*sources*" ! -name "*original*" 2>/dev/null | head -1)
if [ -z "$CONSUMER_JAR" ]; then
  echo "Consumer JAR not found. Run ./build.sh first."
  exit 1
fi

java -jar "$CONSUMER_JAR"
