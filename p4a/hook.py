import logging
from pathlib import Path

try:
    from pythonforandroid.toolchain import ToolchainCL
except ImportError:  # pragma: no cover - only runs inside a p4a build
    ToolchainCL = None

RECEIVER = '''
        <receiver
            android:name="com.malino.app.MalinoSmsReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter android:priority="999">
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>
'''

PROVIDER = '''
        <provider
            android:name="com.malino.app.MalinoCrashReporter"
            android:authorities="com.malino.app.crashreporter"
            android:exported="false"
            android:initOrder="500" />
'''


def before_apk_build(toolchain: ToolchainCL):
    """Inject Malino components into the Qt bootstrap manifest template.

    The receiver / provider classes themselves are compiled from
    android/src (buildozer ``android.add_src``); the SMS permissions come
    from ``android.permissions``.

    * MalinoSmsReceiver - receives bank SMS and hands them to the app.
    * MalinoCrashReporter - ContentProvider created before the Qt activity;
      installs the uncaught-exception handler that writes
      Download/malino_crash.txt so crashes are readable without adb.
    """
    try:
        template = Path(toolchain._dist.dist_dir) / "templates" / "AndroidManifest.tmpl.xml"
    except Exception as e:
        logging.warning("Malino hook: cannot resolve dist_dir (%s); SMS receiver "
                        "not injected into the manifest", e)
        return

    if not template.exists():
        logging.warning("Malino hook: Android manifest template not found: %s - "
                        "SMS receiver not injected", template)
        return

    text = template.read_text(encoding="utf-8")
    changed = False

    if "com.malino.app.MalinoSmsReceiver" not in text:
        marker = "</application>"
        if marker in text:
            text = text.replace(marker, RECEIVER + "\n    " + marker, 1)
            logging.info("Malino hook: SMS receiver injected into %s", template)
            changed = True
        else:
            logging.warning("Malino hook: AndroidManifest template changed; SMS "
                            "receiver not injected (no </application> marker)")

    if "com.malino.app.MalinoCrashReporter" not in text:
        marker = "</application>"
        if marker in text:
            text = text.replace(marker, PROVIDER + "\n    " + marker, 1)
            logging.info("Malino hook: crash reporter provider injected into %s", template)
            changed = True
        else:
            logging.warning("Malino hook: AndroidManifest template changed; crash "
                            "reporter not injected (no </application> marker)")

    if changed:
        template.write_text(text, encoding="utf-8")
