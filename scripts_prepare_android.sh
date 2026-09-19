#!/usr/bin/env bash
# Verify the prerequisites for `./build_android.sh` on a local Linux machine.
set -euo pipefail

fail=0

echo "== Python =="
python3 --version || fail=1

echo "== Java 17 =="
if command -v java >/dev/null; then java -version; else echo "java not found"; fail=1; fi

echo "== Android SDK =="
SDK="${ANDROID_SDK_ROOT:-$HOME/.pyside6_android_deploy/android-sdk}"
if [ -d "$SDK" ]; then echo "SDK: $SDK"; else echo "SDK missing: $SDK"; fail=1; fi

echo "== Android NDK r26b =="
NDK="${ANDROID_NDK_ROOT:-$HOME/.pyside6_android_deploy/android-ndk/android-ndk-r26b}"
if [ -d "$NDK" ]; then echo "NDK: $NDK"; else echo "NDK missing: $NDK"; fail=1; fi

echo "== PySide6 Android wheels =="
if ls android-wheels/PySide6-*android_aarch64.whl >/dev/null 2>&1; then
    ls -lh android-wheels/*android_aarch64.whl
else
    echo "wheels missing (build_android.sh will download them)"
fi

if [ "$fail" -ne 0 ]; then
    echo
    echo "Some prerequisites are missing. Install them manually or let"
    echo "build_android.sh download SDK/NDK via the Qt cross-compile helper."
    exit 1
fi

echo
echo "All prerequisites found. Run: ./build_android.sh"
