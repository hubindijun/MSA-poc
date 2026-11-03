#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== Building server-provider ==="
cd "$SCRIPT_DIR/server-provider"
./mvnw clean package -DskipTests -q
echo "server-provider build done."

echo ""
echo "=== Building server-consumer ==="
cd "$SCRIPT_DIR/server-consumer"
./mvnw clean package -DskipTests -q
echo "server-consumer build done."

echo ""
echo "Build complete! JAR files:"
find "$SCRIPT_DIR" -name "*.jar" -path "*/target/*" ! -name "*sources*" ! -name "*original*"
