#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TOOLS="$ROOT/build/tools"
mkdir -p "$TOOLS"
fetch() {
  local file="$1" url="$2" expected="$3"
  if [[ ! -f "$file" ]]; then
    curl --proto '=https' --proto-redir '=https' -fsSL --retry 3 --connect-timeout 20 "$url" -o "$file"
  fi
  printf '%s  %s\n' "$expected" "$file" | shasum -a 256 -c - >/dev/null
}
fetch "$TOOLS/jacoco-agent.jar" \
  https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.agent/0.8.15/org.jacoco.agent-0.8.15-runtime.jar \
  fb5b0036a0899ea97edfa0fc2c7985b55f3f7c5695028163e5e60d4f3cf6075d
fetch "$TOOLS/jacoco-cli.jar" \
  https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.cli/0.8.15/org.jacoco.cli-0.8.15-nodeps.jar \
  d2b74b20b415163c1f53261e7c5ef4445b788327094208ff821e0c2baf9bc8f1
mkdir -p "$ROOT/build/tests"
javac -d "$ROOT/build/tests" "$ROOT/src/com/jeremykenedy/alloytide/AlloyOptions.java" "$ROOT/src/com/jeremykenedy/alloytide/TileMesh.java" "$ROOT/tests/AlloyTest.java"
java "-javaagent:$TOOLS/jacoco-agent.jar=destfile=$ROOT/build/alloy.exec" \
  -cp "$ROOT/build/tests" com.jeremykenedy.alloytide.AlloyTest
# Only Android-free mesh and settings logic belongs to this host gate.
# Native rendering, provider persistence and lifecycle require separate instrumentation.
java -jar "$TOOLS/jacoco-cli.jar" report "$ROOT/build/alloy.exec" \
  --classfiles "$ROOT/build/tests/com/jeremykenedy/alloytide/AlloyOptions.class" \
  --classfiles "$ROOT/build/tests/com/jeremykenedy/alloytide/TileMesh.class" \
  --sourcefiles "$ROOT/src" --xml "$ROOT/build/alloy-coverage.xml" \
  --html "$ROOT/build/alloy-coverage"
python3 "$ROOT/scripts/check-coverage.py" "$ROOT/build/alloy-coverage.xml"
