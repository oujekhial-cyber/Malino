#!/usr/bin/env python3
"""Patch the python-for-android qt bootstrap for Qt 6.8 compatibility.

python-for-android v2024.01.21 (the release pinned by build_android.sh,
the last one that still builds CPython 3.11 required by the PySide6 6.8
cp311 Android wheels) ships a qt bootstrap whose PythonActivity.java calls
``org.qtproject.qt.android.QtNative.setEnvironmentVariable(String, String)``.
That static method was removed from QtNative in Qt 6.8, so gradle fails with

    error: cannot find symbol
      symbol:   method setEnvironmentVariable(String,String)
      location: class QtNative

This script replaces those calls with a small helper that performs the same
operation via android.system.Os.setenv (which is all the Qt wrapper ever did).
It is idempotent: running it twice changes nothing.
"""

import re
import sys
from pathlib import Path


def main() -> None:
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)

    p4a_dir = Path(sys.argv[1])
    activity = (p4a_dir / "pythonforandroid" / "bootstraps" / "qt" / "build" /
                "src" / "main" / "java" / "org" / "kivy" / "android" /
                "PythonActivity.java")

    if not activity.exists():
        print(f"[patch-p4a] {activity} not found - nothing to patch")
        return

    text = activity.read_text(encoding="utf-8")

    if "MalinoSetEnv" in text:
        print("[patch-p4a] already patched")
        return

    helper = """
    // --- Qt 6.8 compatibility patch (MalinoSetEnv) ---------------------
    // QtNative.setEnvironmentVariable(String, String) was removed from
    // org.qtproject.qt.android.QtNative in Qt 6.8. This helper performs the
    // same operation through android.system.Os.setenv.
    private static void MalinoSetEnv(String key, String value) {
        try {
            android.system.Os.setenv(key, value, true);
        } catch (Exception e) {
            Log.e("Qt bootstrap", "Unable set environment variable:" + key + "=" + value);
        }
    }
    // -------------------------------------------------------------------
"""

    # insert the helper right before onCreate
    text, n1 = re.subn(r"(\n    @Override\n    public void onCreate\(Bundle savedInstanceState\) \{)",
                       helper + r"\1", text, count=1)
    if n1 != 1:
        print("[patch-p4a] ERROR: onCreate not found; aborting without changes")
        sys.exit(1)

    text, n2 = re.subn(r"\bQtNative\.setEnvironmentVariable\(", "MalinoSetEnv(", text)
    print(f"[patch-p4a] {activity.name}: helper inserted, {n2} calls replaced")

    if n2 == 0:
        print("[patch-p4a] WARNING: no QtNative.setEnvironmentVariable calls found "
              "(p4a may already be compatible)")

    activity.write_text(text, encoding="utf-8")


if __name__ == "__main__":
    main()
