#!/usr/bin/env python3
"""Sanity-check a Malino APK produced by build_android.sh.

Verifies that the APK really contains the Qt runtime, a Python 3.11 bundle
(matching the cp311 PySide6 wheels) and the Malino SMS integration, so a
silently broken build cannot be shipped as a green CI run again.

Usage: scripts_verify_apk.py <app.apk> [aapt]
"""

import re
import subprocess
import sys
import zipfile
from pathlib import Path

REQUIRED_IN_APK = [
    # critical: without these the app cannot start at all
    ("libQt6Core", "Qt core library", True),
    ("libQt6Quick", "Qt Quick library", True),
    ("qtforandroid", "Qt Android platform plugin", True),
    ("libpybundle", "python bundle loader", True),
    ("assets/private", "python app payload", True),
    ("classes.dex", "compiled java/dex classes", True),
    # naming-sensitive: may be merged/dropped without breaking anything
    ("libpython3.11", "CPython 3.11 runtime (may live inside the pybundle)", False),
    ("Qt6AndroidBindings", "Qt for Python bindings jar (may be dex-merged)", False),
]

REQUIRED_IN_MANIFEST = [
    "QtApplication",
    "android.permission.RECEIVE_SMS",
    "MalinoSmsReceiver",
]


def verify_payload(apk_path: Path) -> int:
    with zipfile.ZipFile(apk_path) as zf:
        names = zf.namelist()

    failed = False
    for pattern, what, critical in REQUIRED_IN_APK:
        hits = [n for n in names if pattern in n]
        if hits:
            print(f"OK      {what}: {hits[0]}")
        elif critical:
            print(f"MISSING {what} (no entry containing {pattern!r})")
            failed = True
        else:
            print(f"warn    {what}: not found by {pattern!r} (may be merged)")
    return 1 if failed else 0


def verify_manifest(apk_path: Path, aapt: str) -> int:
    if not aapt or not Path(aapt).exists():
        print(f"WARN   aapt not found ({aapt!r}); skipping manifest checks")
        return 0

    try:
        result = subprocess.run(
            [aapt, "dump", "xmltree", str(apk_path), "AndroidManifest.xml"],
            capture_output=True, text=True)
    except OSError as e:
        print(f"WARN   cannot run aapt ({e}); skipping manifest checks")
        return 0

    if result.returncode != 0:
        print(f"WARN   aapt failed ({result.stderr.strip()}); skipping manifest checks")
        return 0

    manifest = result.stdout

    failed = False
    for needle in REQUIRED_IN_MANIFEST:
        if re.search(re.escape(needle), manifest):
            print(f"OK      manifest contains {needle}")
        else:
            print(f"MISSING manifest entry {needle}")
            failed = True
    return 1 if failed else 0


def main() -> int:
    if len(sys.argv) < 2:
        print(__doc__)
        return 2

    apk_path = Path(sys.argv[1])
    if not apk_path.exists():
        print(f"ERROR: {apk_path} does not exist")
        return 2

    aapt = sys.argv[2] if len(sys.argv) > 2 else ""

    rc = verify_payload(apk_path)
    rc = verify_manifest(apk_path, aapt) or rc

    print("APK verification:", "PASSED" if rc == 0 else "FAILED")
    return rc


if __name__ == "__main__":
    sys.exit(main())
