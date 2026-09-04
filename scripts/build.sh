#!/usr/bin/env bash
# Local and CI entry point. The workflow only installs the Android toolchain
# and signing secrets, then invokes this script the same way a developer would.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root/dw2003-dual-screen"
if [[ $# -eq 0 ]]; then
  set -- :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
fi
exec ./gradlew "$@"
