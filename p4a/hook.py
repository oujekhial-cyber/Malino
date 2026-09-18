from pathlib import Path
from pythonforandroid.toolchain import ToolchainCL

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
    # Qt's Android bootstrap renders AndroidManifest.xml from this template.
    # Inject the SMS receiver before the template is rendered.
    template = Path(toolchain._dist.dist_dir) / "templates" / "AndroidManifest.tmpl.xml"
    if not template.exists():
        raise RuntimeError(f"Malino: Android manifest template not found: {template}")
    text = template.read_text(encoding="utf-8")
    if "com.malino.app.MalinoSmsReceiver" in text:
        return
    marker = "</application>"
    if marker not in text:
        raise RuntimeError("Malino: AndroidManifest template changed; cannot inject SMS receiver safely")
    text = text.replace(marker, RECEIVER + "\n        " + marker, 1)
    template.write_text(text, encoding="utf-8")
