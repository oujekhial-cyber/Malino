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

# Target ABI: aarch64 (real phones) or x86_64 (GitHub Actions emulator test).
ARCH="${ARCH:-aarch64}"
echo "==> Building for ABI: $ARCH"

echo "==> Installing build dependencies"
python3 -m pip install -U "PySide6==${PYSIDE_VERSION}" "shiboken6==${PYSIDE_VERSION}"
# The Qt deploy tool imports these without declaring them anywhere
# (PySide6 wheel metadata only declares shiboken6/Essentials/Addons):
#   android_utilities.py -> packaging, tqdm
#   android_helper.py    -> jinja2
#   android_config.py    -> pkginfo
# python-for-android v2024.01.21 (pinned below) additionally needs
# appdirs, colorama, jinja2, sh<2, build, toml, packaging, setuptools,
# and buildozer 1.5.0 uses packaging without declaring it.
python3 -m pip install -U "buildozer==1.5.0" "cython<3" \
    "packaging" "tqdm" "jinja2" "pkginfo" \
    "appdirs" "colorama>=0.3.3" "toml" "build" \
    "sh>=1.10,<2.0" "setuptools"

# ---------------------------------------------------------------------------
# PySide6 Android wheels (aarch64) - pinned versions
# ---------------------------------------------------------------------------
WHEEL_BASE="https://download.qt.io/official_releases/QtForPython"
PSWHEEL_NAME="PySide6-${PYSIDE_WHEEL_VERSION}-cp311-cp311-android_${ARCH}.whl"
SWWHEEL_NAME="shiboken6-${PYSIDE_WHEEL_VERSION}-cp311-cp311-android_${ARCH}.whl"
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
    # the helper imports git (GitPython), jinja2, packaging, tqdm
    python3 -m pip install -r "${HOME}/pyside-setup/requirements.txt"
    python3 -m pip install -r "${HOME}/pyside-setup/tools/cross_compile_android/requirements.txt"
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
# own process group so we can kill the tool AND any buildozer child it may
# already have spawned when we stop it (otherwise the orphaned buildozer
# keeps running and races against our own phase-3 buildozer)
setsid pyside6-android-deploy "${DEPLOY_ARGS[@]}" --keep-deployment-files --force &
DEPLOY_PID=$!

for i in $(seq 1 240); do
    if ! kill -0 "$DEPLOY_PID" 2>/dev/null; then break; fi
    if [ -f buildozer.spec ] && grep -q "p4a.local_recipes" buildozer.spec 2>/dev/null; then
        echo "==> buildozer.spec generated; stopping phase 1"
        sleep 3
        kill -TERM -- -"$DEPLOY_PID" 2>/dev/null || true
        sleep 2
        kill -KILL -- -"$DEPLOY_PID" 2>/dev/null || true
        break
    fi
    sleep 2
done
pkill -9 -f "python.* -m buildozer" 2>/dev/null || true
wait "$DEPLOY_PID" 2>/dev/null || true

# The deploy tool swallows errors (prints them, then exits 0) and the build
# loop below would silently produce a broken APK if phase 1 was incomplete.
# Verify the generated spec is really the Qt-specific one before continuing.
if [ ! -f buildozer.spec ]; then
    echo "خطا: buildozer.spec ساخته نشد — خروجی فاز 1 را بالا ببینید"; exit 2
fi
grep -Eq "^p4a\.bootstrap[[:space:]]*=[[:space:]]*qt" buildozer.spec \
    || { echo "خطا: p4a.bootstrap=qt در buildozer.spec نیست (spec پیش‌فرض است)"; exit 2; }
grep -Eq "^p4a\.local_recipes[[:space:]]*=[[:space:]]*\S+" buildozer.spec \
    || { echo "خطا: p4a.local_recipes خالی است — recipeهای Qt ساخته نشده‌اند"; exit 2; }
grep -q "Qt6AndroidBindings" buildozer.spec \
    || { echo "خطا: android.add_jars شامل Qt6AndroidBindings نیست — استخراج jar شکست خورده"; exit 2; }
grep -Eq "^android\.add_jars[[:space:]]*=[[:space:]]*\S+" buildozer.spec \
    || { echo "خطا: android.add_jars خالی است"; exit 2; }
[ -f deployment/recipes/PySide6/__init__.py ] \
    || { echo "خطا: recipe PySide6 در deployment/recipes نیست"; exit 2; }
echo "==> فاز 1 کامل و سالم است"

# ---------------------------------------------------------------------------
# Phase 2: patch the generated spec
# ---------------------------------------------------------------------------
echo "==> Phase 2: patching buildozer.spec"
python3 scripts_patch_buildozer.py buildozer.spec --p4a-ref "$P4A_REF" --arch "$ARCH"

# ---------------------------------------------------------------------------
# Phase 3: build the APK directly with buildozer + the patched spec
# ---------------------------------------------------------------------------
echo "==> Phase 3: building APK with buildozer (p4a ${P4A_REF})"

# Pre-clone python-for-android at the pinned ref and patch it for Qt 6.8:
# buildozer skips its own clone when the url+branch already match, so our
# patched copy wins. (The qt bootstrap of p4a v2024.01.21 still calls
# QtNative.setEnvironmentVariable, which was removed in Qt 6.8 -> gradle
# compile error.)
P4A_DIR=".buildozer/android/platform/python-for-android"
if [ ! -d "$P4A_DIR/.git" ]; then
    mkdir -p "$(dirname "$P4A_DIR")"
    git clone -q -b "$P4A_REF" --single-branch \
        https://github.com/kivy/python-for-android "$P4A_DIR"
fi
git -C "$P4A_DIR" checkout -q -B "$P4A_REF" 2>/dev/null || true
git -C "$P4A_DIR" clean -qfd || true
python3 scripts_patch_p4a.py "$P4A_DIR"

python3 -m buildozer -v android debug || {
    # If buildozer replaced our patched clone with a fresh one (or the patch
    # raced with its clone), patch again and retry exactly once.
    echo "==> first buildozer attempt failed; re-applying the p4a patch and retrying"
    if [ -d "$P4A_DIR/.git" ]; then
        git -C "$P4A_DIR" checkout -q -B "$P4A_REF" 2>/dev/null || true
        python3 scripts_patch_p4a.py "$P4A_DIR"
    fi
    python3 -m buildozer -v android debug
}

echo "==> Artifacts:"
find . -maxdepth 3 -type f \( -name '*.apk' -o -name '*.aab' \) -print
