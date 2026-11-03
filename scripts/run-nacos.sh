#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

cd "$SCRIPT_DIR/nacos"
docker compose up -d

echo "Nacos starting..."
echo "Console: http://localhost:8848/nacos (nacos/nacos)"
