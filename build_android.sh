#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
python3 -m pip install -U buildozer cython qtpip
if [ ! -d "$HOME/pyside-setup" ]; then git clone --depth 1 --branch 6.8 https://code.qt.io/pyside/pyside-setup "$HOME/pyside-setup"; fi
python3 "$HOME/pyside-setup/tools/cross_compile_android/main.py" --download-only --skip-update --auto-accept-license
mkdir -p android-wheels
qtpip download PySide6 --android --arch aarch64 -d android-wheels
PSWHEEL=$(ls -1 android-wheels/PySide6-*.whl | head -n1)
SWWHEEL=$(ls -1 android-wheels/shiboken6-*.whl | head -n1)
if [ -z "${PSWHEEL}" ] || [ -z "${SWWHEEL}" ]; then echo "Android PySide6/Shiboken wheels پیدا نشدند"; exit 2; fi

echo "PySide wheel: $PSWHEEL"
echo "Shiboken wheel: $SWWHEEL"

# pyside6-android-deploy generates the Buildozer configuration and uses the
# Qt bootstrap. If buildozer.spec already exists, the deploy tool reuses it.
# This project ships a spec with the Android Java receiver and p4a hook.
python3 -m pip install -U PySide6==6.8.*
pyside6-android-deploy --name Malino --wheel-pyside="$PSWHEEL" --wheel-shiboken="$SWWHEEL" --config-file pysidedeploy.spec --force

find . -maxdepth 3 -type f \( -name '*.apk' -o -name '*.aab' \) -print
