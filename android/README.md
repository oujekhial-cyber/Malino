Android Java sources are copied with `android.add_src` in buildozer.spec.
The SMS receiver is registered in the generated AndroidManifest through `p4a/hook.py` before the Qt bootstrap renders the manifest.
