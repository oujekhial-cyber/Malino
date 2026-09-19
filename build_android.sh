#!/usr/bin/env bash
# Build the Malino Android APK with the official Qt tooling
# (pyside6-android-deploy + buildozer/python-for-android).
#
# The Qt deploy tool generates its own buildozer.spec in a temporary state and
# deletes it at the end of the run, and it hardcodes `p4a.branch = develop`.
# Cloning python-for-android `develop` today builds Python 3.14, while the
# PySide6 6.8 Android wheels are cp311 binaries -> the produced APK contains a
# Python that cannot load PySide6 and the app dies instantly on the device.
#
# Therefore this script runs the deployment in three phases:
#   1. Run pyside6-android-deploy just long enough to generate the correct
#      buildozer.spec (Qt bootstrap, jars, recipes) and keep the files.
#   2. Patch the generated buildozer.spec: pin p4a to a release that still
#      builds Python 3.11 (the version the wheels were built for) and restore
#      the SMS permissions/receiver that the generated spec does not carry.
#   3. Run buildozer directly with the patched spec to produce the final APK.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

PYSIDE_VERSION="6.8.2"
PYSIDE_WHEEL_VERSION="6.8.2.1-6.8.2"
P4A_REF="v2024.01.21"   # last p4a release line with the qt bootstrap + python 3.11

echo "==> Installing build dependencies"
python3 -m pip install -U "PySide6==${PYSIDE_VERSION}" "shiboken6==${PYSIDE_VERSION}"
python3 -m pip install -U "buildozer==1.5.0" "cython<3" "GitPython" "pkginfo"

# ---------------------------------------------------------------------------
# PySide6 Android wheels (aarch64) - pinned versions
# ---------------------------------------------------------------------------
WHEEL_BASE="https://download.qt.io/official_releases/QtForPython"
PSWHEEL_NAME="PySide6-${PYSIDE_WHEEL_VERSION}-cp311-cp311-android_aarch64.whl"
SWWHEEL_NAME="shiboken6-${PYSIDE_WHEEL_VERSION}-cp311-cp311-android_aarch64.whl"
mkdir -p android-wheels
if [ -z "${PSWHEEL:-}" ] || [ -z "${SWWHEEL:-}" ]; then
    echo "==> Downloading PySide6 Android wheels (${PYSIDE_WHEEL_VERSION})"
    [ -f "android-wheels/${PSWHEEL_NAME}" ] || \
        wget -nv "${WHEEL_BASE}/pyside6/${PSWHEEL_NAME}" -O "android-wheels/${PSWHEEL_NAME}"
    [ -f "android-wheels/${SWWHEEL_NAME}" ] || \
        wget -nv "${WHEEL_BASE}/shiboken6/${SWWHEEL_NAME}" -O "android-wheels/${SWWHEEL_NAME}"
    PSWHEEL="$ROOT/android-wheels/${PSWHEEL_NAME}"
    SWWHEEL="$ROOT/android-wheels/${SWWHEEL_NAME}"
fi
[ -f "$PSWHEEL" ] && [ -f "$SWWHEEL" ] || { echo "Android PySide6/Shiboken wheels پیدا نشد"; exit 2; }

# ---------------------------------------------------------------------------
# SDK / NDK (reuse environment if provided, otherwise use the Qt tool cache)
# ---------------------------------------------------------------------------
DEPLOY_CACHE="${HOME}/.pyside6_android_deploy"
if [ -z "${ANDROID_SDK_ROOT:-}" ]; then
    ANDROID_SDK_ROOT="${DEPLOY_CACHE}/android-sdk"
fi
if [ -z "${ANDROID_NDK_ROOT:-}" ]; then
    ANDROID_NDK_ROOT="${DEPLOY_CACHE}/android-ndk/android-ndk-r26b"
fi
if [ ! -d "$ANDROID_SDK_ROOT" ] || [ ! -d "$ANDROID_NDK_ROOT" ]; then
    echo "==> SDK/NDK not found; downloading via the Qt cross-compile helper"
    if [ ! -d "${HOME}/pyside-setup" ]; then
        git clone --depth 1 --branch 6.8 https://code.qt.io/pyside/pyside-setup "${HOME}/pyside-setup"
    fi
    python3 "${HOME}/pyside-setup/tools/cross_compile_android/main.py" \
        --download-only --skip-update --auto-accept-license
fi
[ -d "$ANDROID_SDK_ROOT" ] || { echo "Android SDK not found at $ANDROID_SDK_ROOT"; exit 2; }
[ -d "$ANDROID_NDK_ROOT" ] || { echo "Android NDK not found at $ANDROID_NDK_ROOT"; exit 2; }
export ANDROID_SDK_ROOT ANDROID_NDK_ROOT
if [ -z "${JAVA_HOME:-}" ]; then
    for J in /usr/lib/jvm/java-17-openjdk-amd64 /usr/lib/jvm/java-17-openjdk; do
        [ -x "$J/bin/java" ] && JAVA_HOME="$J" && break
    done
fi
[ -n "${JAVA_HOME:-}" ] && export PATH="${JAVA_HOME}/bin:$PATH"

DEPLOY_ARGS=(--name Malino
             --wheel-pyside="$PSWHEEL"
             --wheel-shiboken="$SWWHEEL"
             --ndk-path="$ANDROID_NDK_ROOT"
             --sdk-path="$ANDROID_SDK_ROOT"
             --config-file="$ROOT/pysidedeploy.spec")

# ---------------------------------------------------------------------------
# Phase 1: generate the Qt buildozer.spec (and recipes/jars), then stop the
# deploy tool before the hours-long build: it would build against p4a develop.
# ---------------------------------------------------------------------------
echo "==> Phase 1: generating buildozer.spec via pyside6-android-deploy"
rm -f buildozer.spec
pyside6-android-deploy "${DEPLOY_ARGS[@]}" --keep-deployment-files --force &
DEPLOY_PID=$!

for i in $(seq 1 240); do
    if ! kill -0 "$DEPLOY_PID" 2>/dev/null; then break; fi
    if [ -f buildozer.spec ] && grep -q "p4a.local_recipes" buildozer.spec 2>/dev/null; then
        echo "==> buildozer.spec generated; stopping phase 1"
        kill "$DEPLOY_PID" 2>/dev/null || true
        sleep 2
        kill -9 "$DEPLOY_PID" 2>/dev/null || true
        break
    fi
    sleep 2
done
wait "$DEPLOY_PID" 2>/dev/null || true

[ -f buildozer.spec ] || { echo "buildozer.spec ساخته نشد"; exit 2; }

# ---------------------------------------------------------------------------
# Phase 2: patch the generated spec
# ---------------------------------------------------------------------------
echo "==> Phase 2: patching buildozer.spec"
python3 scripts_patch_buildozer.py buildozer.spec --p4a-ref "$P4A_REF"

# ---------------------------------------------------------------------------
# Phase 3: build the APK directly with buildozer + the patched spec
# ---------------------------------------------------------------------------
echo "==> Phase 3: building APK with buildozer (p4a ${P4A_REF})"
python3 -m buildozer -v android debug

echo "==> Artifacts:"
find . -maxdepth 3 -type f \( -name '*.apk' -o -name '*.aab' \) -print
