#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
bash -n "$ROOT/build.sh"
bash "$ROOT/scripts/test-coverage.sh"
bash "$ROOT/scripts/test-python-coverage.sh"
bash "$ROOT/scripts/check-style.sh"
