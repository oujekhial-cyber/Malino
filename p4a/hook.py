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


def before_apk_build(toolchain: ToolchainCL):
    """Inject the Malino SMS receiver into the Qt bootstrap manifest template.

    The receiver itself is compiled from android/src (buildozer
    ``android.add_src``); its permissions come from ``android.permissions``.
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
    if "com.malino.app.MalinoSmsReceiver" in text:
        return

    marker = "</application>"
    if marker not in text:
        logging.warning("Malino hook: AndroidManifest template changed; SMS "
                        "receiver not injected (no </application> marker)")
        return

    text = text.replace(marker, RECEIVER + "\n    " + marker, 1)
    template.write_text(text, encoding="utf-8")
    logging.info("Malino hook: SMS receiver injected into %s", template)
