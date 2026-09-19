#!/usr/bin/env python3
"""Patch the python-for-android qt bootstrap for Qt 6.8 compatibility.

python-for-android v2024.01.21 (the release pinned by build_android.sh,
the last one that still builds CPython 3.11 required by the PySide6 6.8
cp311 Android wheels) ships a qt bootstrap with three startup problems:

1. PythonActivity.java calls ``org.qtproject.qt.android.QtNative
   .setEnvironmentVariable(String, String)``. That static method was
   removed from QtNative in Qt 6.8, so gradle fails with

       error: cannot find symbol
         symbol:   method setEnvironmentVariable(String,String)
         location: class QtNative

   This script replaces those calls with a small helper that performs the
   same operation via android.system.Os.setenv (which is all the Qt
   wrapper ever did).

2. The app crashes on device at startup (before Python starts) with

       android.content.res.Resources$NotFoundException:
       String array resource ID #0x0
           at org.qtproject.qt.android.QtLoader.getBundledLibs
           at org.qtproject.qt.android.QtLoader.loadQtLibraries
           at org.qtproject.qt.android.QtActivityBase.onCreate

   ``QtLoader.getBundledLibs`` (Qt 6.8, qtbase QtLoader.java) does
   ``resources.getIdentifier("bundled_libs", "array", packageName)`` and
   in 6.8 does NOT catch the resulting ``Resources$NotFoundException``.
   The ``bundled_libs`` array is rendered into the APK from the qt
   bootstrap template ``build/templates/libs.tmpl.xml`` - but the
   v2024.01.21 template predates that resource, so it never exists and
   ``getIdentifier`` returns 0.  Newer p4a (develop) ships the fix: an
   empty placeholder array with the comment "The bundled_libs placeholder
   is needed for QtLoader.java. Otherwise the application will crash."
   This script backports that placeholder into the template.

3. The ``libs.xml`` resource rendered by ``bootstraps/common/build/build.py``
   (arrays ``qt_libs`` / ``load_local_libs``) is the list of "bundled
   libraries" that ``QtLoader.java`` in Qt6AndroidBindings.jar calls
   ``System.load()`` on from the APK's native library directory at app
   startup.  That list comes from ``pysidedeploy.spec`` (``[qt] modules``)
   and the wheel dependency XMLs, but the PySide6 p4a recipe only bundles
   a library if it actually exists in the wheel.  Any module whose
   ``libQt6<Module>_<arch>.so`` (or ``Qt<Module>.abi3.so``) is not in the
   wheel therefore ends up listed in ``libs.xml`` but missing from the
   APK, and the app dies at startup with ``java.lang.UnsatisfiedLinkError``
   before Python even starts.  This script patches ``build.py`` to
   reconcile the lists with the libraries that are really bundled
   (gradle packages ``libs/<arch>`` into the APK ``lib/`` dir) and drops
   the entries whose files are absent.

Additionally, PythonActivity logs the actually-bundled native libraries at
startup (``bundled_libs (N): [...]``) so such crashes are debuggable from
logcat.

All patches are idempotent: running the script twice changes nothing.
"""

import re
import sys
from pathlib import Path


def _patch_java_setenv(activity: Path, text: str) -> str:
    """Replace QtNative.setEnvironmentVariable with the MalinoSetEnv helper."""
    if "MalinoSetEnv" in text:
        print("[patch-p4a] PythonActivity.java: setenv patch already applied")
        return text

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

    # replace the calls first, then insert the helper, so the replacement
    # does not touch the helper's own comment text
    text, n2 = re.subn(r"\bQtNative\.setEnvironmentVariable\(", "MalinoSetEnv(", text)

    # insert the helper right before onCreate
    text, n1 = re.subn(r"(\n    @Override\n    public void onCreate\(Bundle savedInstanceState\) \{)",
                       helper + r"\1", text, count=1)
    if n1 != 1:
        print("[patch-p4a] ERROR: onCreate not found; aborting without changes")
        sys.exit(1)

    print(f"[patch-p4a] {activity.name}: setenv helper inserted, {n2} calls replaced")

    if n2 == 0:
        print("[patch-p4a] WARNING: no QtNative.setEnvironmentVariable calls found "
              "(p4a may already be compatible)")
    return text


def _patch_java_bundled_libs_log(activity: Path, text: str) -> str:
    """Log the actually-bundled native libraries at startup (diagnostics)."""
    if "MalinoLogBundledLibs" in text:
        print("[patch-p4a] PythonActivity.java: bundled_libs log already applied")
        return text

    helper = """
    // --- Malino bundled_libs diagnostics (MalinoLogBundledLibs) ---------
    // Log the native libraries actually bundled in the APK (the exact set
    // gradle places into the APK lib/ dir). On a startup crash such as
    // java.lang.UnsatisfiedLinkError raised by QtLoader.java, compare this
    // log with res/values/libs.xml to see which bundled library is missing.
    private static void MalinoLogBundledLibs(Activity activity) {
        try {
            File libsDir = new File(activity.getApplicationInfo().nativeLibraryDir);
            String[] entries = libsDir.list();
            if (entries == null) {
                entries = new String[0];
            }
            java.util.Arrays.sort(entries);
            Log.i(TAG, "bundled_libs (" + entries.length + "): "
                    + java.util.Arrays.toString(entries));
        } catch (Exception e) {
            Log.e(TAG, "bundled_libs: unable to list native library dir", e);
        }
    }
    // ---------------------------------------------------------------------
"""

    # insert the method right before onCreate (same anchor as MalinoSetEnv)
    text, n1 = re.subn(r"(\n    @Override\n    public void onCreate\(Bundle savedInstanceState\) \{)",
                       helper + r"\1", text, count=1)
    if n1 != 1:
        print("[patch-p4a] ERROR: onCreate not found (bundled_libs log); "
              "aborting without changes")
        sys.exit(1)

    # call it at the very start of onCreate so the log is visible early
    text, n2 = re.subn(r"Log\.v\(TAG, \"Ready to unpack\"\);",
                       "Log.v(TAG, \"Ready to unpack\");\n        MalinoLogBundledLibs(this);",
                       text, count=1)
    if n2 != 1:
        print("[patch-p4a] WARNING: 'Ready to unpack' log line not found; "
              "bundled_libs log method added but not called")

    print(f"[patch-p4a] {activity.name}: bundled_libs diagnostics added")
    return text


def patch_python_activity(activity: Path) -> None:
    if not activity.exists():
        print(f"[patch-p4a] WARNING: {activity} not found; skipping Java patches")
        return

    text = activity.read_text(encoding="utf-8")
    text = _patch_java_setenv(activity, text)
    text = _patch_java_bundled_libs_log(activity, text)
    activity.write_text(text, encoding="utf-8")


def patch_libs_tmpl_bundled_libs(template: Path) -> None:
    """Backport the bundled_libs placeholder required by Qt 6.8 QtLoader.

    QtLoader.getBundledLibs (qtbase 6.8) reads the ``bundled_libs`` string
    array from the app resources without catching Resources$NotFoundException
    (the catch was only added in newer Qt). The v2024.01.21 qt bootstrap
    template never defines that array, so getIdentifier() returns 0 and the
    app crashes at startup with:

        android.content.res.Resources$NotFoundException:
        String array resource ID #0x0
            at org.qtproject.qt.android.QtLoader.getBundledLibs

    Newer p4a (develop) defines an empty placeholder for exactly this
    reason ("Otherwise the application will crash."). Extra bundled
    libraries can be added to the array via buildozer android.add_libs_*.
    """
    if not template.exists():
        print(f"[patch-p4a] WARNING: {template} not found; "
              "skipping the bundled_libs placeholder patch")
        return

    text = template.read_text(encoding="utf-8")

    if 'name="bundled_libs"' in text:
        print("[patch-p4a] libs.tmpl.xml: bundled_libs placeholder already present")
        return

    anchor = "<resources>"
    if anchor not in text:
        print("[patch-p4a] ERROR: <resources> anchor not found in "
              f"{template} (p4a version changed?); aborting without changes")
        sys.exit(1)

    placeholder = (
        "<resources>\n"
        "\n"
        "    <!--\n"
        "    The bundled_libs placeholder is needed for QtLoader.java. "
        "Otherwise the\n"
        "    application will crash (Resources$NotFoundException: String\n"
        "    array resource ID #0x0, Qt 6.8 does not catch the missing\n"
        "    resource). Adding extra libraries can be done through\n"
        "    buildozer directly with the android.add_libs_* options.\n"
        "    -->\n"
        '    <array name="bundled_libs">\n'
        "    </array>\n"
    )
    text = text.replace(anchor, placeholder, 1)

    template.write_text(text, encoding="utf-8")
    print(f"[patch-p4a] {template.name}: bundled_libs placeholder added "
          "(crash fix for QtLoader.getBundledLibs)")


def patch_build_py_bundled_libs(build_py: Path) -> None:
    """Reconcile libs.xml library lists with the actually bundled .so files."""
    if not build_py.exists():
        print(f"[patch-p4a] WARNING: {build_py} not found; "
              "skipping the bundled_libs fix")
        return

    text = build_py.read_text(encoding="utf-8")

    if "malino_reconcile_bundled_libs" in text:
        print("[patch-p4a] build.py: bundled_libs fix already applied")
        return

    # Insert the reconciliation helper before the first function definition.
    function = '''# --- Malino bundled_libs fix (MalinoBundledLibs) -------------------------
# QtLoader.java in Qt6AndroidBindings.jar calls System.load() on every entry
# of the qt_libs / load_local_libs arrays in res/values/libs.xml from the
# APK's native library directory at app startup.  Those lists come from
# pysidedeploy.spec ([qt] modules) and the wheel dependency XMLs, while the
# PySide6 p4a recipe only bundles a library if it exists in the wheel.  A
# module whose libQt6<Module>_<arch>.so (or Qt<Module>.abi3.so) is not in
# the wheel therefore ends up listed in libs.xml but missing from the APK,
# and the app dies at startup with java.lang.UnsatisfiedLinkError before
# Python even starts.  Reconcile the lists with the libraries that are
# really bundled: gradle packages libs/<arch> (and src/main/jniLibs/<arch>)
# into the APK lib/ dir, and build.py runs with the dist dir as CWD after
# the recipes and assemble_distribution() have filled it.
def malino_reconcile_bundled_libs(qt_libs, load_local_libs, arch):
    lib_dirs = [join("libs", arch), join("src", "main", "jniLibs", arch)]
    bundled_libs = set()
    for lib_dir in lib_dirs:
        if exists(lib_dir):
            bundled_libs.update(listdir(lib_dir))
    if not bundled_libs:
        # No ground truth available (nothing distributed yet) - keep the
        # upstream behavior instead of guessing.
        return qt_libs, load_local_libs

    kept_qt_libs = []
    dropped_qt_libs = []
    for module in qt_libs:
        # each qt_lib (module) makes QtLoader load both the C++ Qt library
        # (libQt6<Module>_<arch>.so) and the PySide6 module (Qt<Module>.abi3.so)
        needed = ("libQt6{}_{}.so".format(module, arch),
                  "Qt{}.abi3.so".format(module))
        if module == "Qml":
            # the template also emits libpyside6qml.abi3.so for the Qml module
            needed = needed + ("libpyside6qml.abi3.so",)
        if all(lib in bundled_libs for lib in needed):
            kept_qt_libs.append(module)
        else:
            dropped_qt_libs.append(module)

    kept_local_libs = []
    dropped_local_libs = []
    for lib in load_local_libs:
        if "lib{}_{}.so".format(lib, arch) in bundled_libs:
            kept_local_libs.append(lib)
        else:
            dropped_local_libs.append(lib)

    if dropped_qt_libs or dropped_local_libs:
        print("[malino] bundled_libs fix: libs.xml lists libraries that are "
              "not bundled in the APK - dropping them so QtLoader.java does "
              "not System.load() missing files (that crashes the app at "
              "startup): qt_libs={} load_local_libs={}".format(
                  dropped_qt_libs, dropped_local_libs))
    return kept_qt_libs, kept_local_libs
# --- end Malino bundled_libs fix (MalinoBundledLibs) ---------------------
'''

    anchor = "def get_dist_info_for(key, error_if_missing=True):"
    if anchor not in text:
        print("[patch-p4a] ERROR: build.py anchor not found "
              "(p4a version changed?); aborting without changes")
        sys.exit(1)
    text = text.replace(anchor, function + anchor, 1)

    # Call the helper where the lists are prepared for the libs.xml render.
    render_anchor = ("arch = get_dist_info_for(\"archs\")[0]\n"
                     "        render(\n"
                     "            'libs.tmpl.xml',")
    if render_anchor not in text:
        print("[patch-p4a] ERROR: libs.xml render anchor not found "
              "(p4a version changed?); aborting without changes")
        sys.exit(1)
    replacement = ("arch = get_dist_info_for(\"archs\")[0]\n"
                   "        qt_libs, load_local_libs = malino_reconcile_bundled_libs(\n"
                   "            qt_libs, load_local_libs, arch)\n"
                   "        render(\n"
                   "            'libs.tmpl.xml',")
    text = text.replace(render_anchor, replacement, 1)

    build_py.write_text(text, encoding="utf-8")
    print(f"[patch-p4a] {build_py.name}: bundled_libs reconciliation added")


def main() -> None:
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)

    p4a_dir = Path(sys.argv[1])

    activity = (p4a_dir / "pythonforandroid" / "bootstraps" / "qt" / "build" /
                "src" / "main" / "java" / "org" / "kivy" / "android" /
                "PythonActivity.java")
    libs_tmpl = (p4a_dir / "pythonforandroid" / "bootstraps" / "qt" / "build" /
                 "templates" / "libs.tmpl.xml")
    build_py = (p4a_dir / "pythonforandroid" / "bootstraps" / "common" /
                "build" / "build.py")

    patch_python_activity(activity)
    patch_libs_tmpl_bundled_libs(libs_tmpl)
    patch_build_py_bundled_libs(build_py)


if __name__ == "__main__":
    main()
